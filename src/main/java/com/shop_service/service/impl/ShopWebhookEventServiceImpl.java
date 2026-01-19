package com.shop_service.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop_service.common.constant.LockServiceType;
import com.shop_service.common.constant.ShopWebhookEventType;
import com.shop_service.common.constant.ShopWebhookStatus;
import com.shop_service.common.core.LockKeyProduce;
import com.shop_service.common.core.RedissonLockExecutor;
import com.shop_service.common.core.RespPageConvert;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopWebhookEventMapper;
import com.shop_service.model.entity.ShopWebhookEvent;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.request.AdminShopWebhookEventPageQuery;
import com.shop_service.model.response.AdminShopWebhookEventVo;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * 商户回调事件服务实现类
 *
 * @author 啊祖
 * @date 2026-01-14 16:37
 **/
@Slf4j
@Service
public class ShopWebhookEventServiceImpl extends ServiceImpl<ShopWebhookEventMapper, ShopWebhookEvent> implements IShopWebhookEventService {
    @Resource
    private RedissonLockExecutor redissonLockExecutor;

    @Resource(name = "applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    @Override
    public void publish(ShopInfo shopInfo, ShopWebhookEventType eventType, Object payload) {
        // 构建回调事件
        ShopWebhookEvent event = new ShopWebhookEvent();
        event.setShopId(shopInfo.getId());
        event.setShopNo(shopInfo.getNo());
        event.setEventId("event-" + UUID.randomUUID());
        event.setEventType(eventType.getValue());
        event.setWebhookUrl(shopInfo.getWebhookUrl());
        event.setPayload(JSON.toJSONString(payload));

        // 是否等待扫描发送
        boolean canSend = Boolean.TRUE.equals(shopInfo.getEnabled())
                && Boolean.TRUE.equals(shopInfo.getWebhookEnabled())
                && shopInfo.getWebhookUrl() != null;

        if (canSend) {
            event.setStatus(ShopWebhookStatus.PENDING.getValue());
            event.setRetryCount(0);
            event.setNextRetryTime(LocalDateTime.now());
        } else {
            event.setStatus(ShopWebhookStatus.ABANDONED.getValue());
            event.setRetryCount(0);
            event.setLastError("商户被停用, 或者商户未开启回调, 或者设置未设置回调url");
        }

        baseMapper.insert(event);
    }

    @Override
    public List<Long> getDueEventList(LocalDateTime now, int limit) {
        return baseMapper.selectDueEventIdList(
                now,
                limit,
                ShopWebhookStatus.PENDING.getValue(),
                ShopWebhookStatus.FAILED_RETRY.getValue()
        );
    }

    @Override
    public boolean markSending(Long id, LocalDateTime now) {
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        return redissonLockExecutor.execute(lock, () -> {
            // 更新
            LambdaUpdateWrapper<ShopWebhookEvent> uw = new LambdaUpdateWrapper<>();
            uw.eq(ShopWebhookEvent::getId, id);
            uw.in(ShopWebhookEvent::getStatus,
                    ShopWebhookStatus.PENDING.getValue(),
                    ShopWebhookStatus.FAILED_RETRY.getValue()
            );
            uw.and(w -> w.isNull(ShopWebhookEvent::getNextRetryTime).or().le(ShopWebhookEvent::getNextRetryTime, now));

            uw.set(ShopWebhookEvent::getStatus, ShopWebhookStatus.SENDING.getValue());
            uw.set(ShopWebhookEvent::getLastSendTime, now);
            uw.set(ShopWebhookEvent::getUpdateTime, LocalDateTime.now());

            return this.update(uw);
        });
    }

    @Override
    public void markSuccess(Long id) {
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        redissonLockExecutor.execute(lock, () -> {
            // 更新
            LambdaUpdateWrapper<ShopWebhookEvent> uw = new LambdaUpdateWrapper<>();
            uw.eq(ShopWebhookEvent::getId, id);

            uw.set(ShopWebhookEvent::getStatus, ShopWebhookStatus.SUCCESS.getValue());
            uw.set(ShopWebhookEvent::getNextRetryTime, null);
            uw.set(ShopWebhookEvent::getLastError, null);
            uw.set(ShopWebhookEvent::getUpdateTime, LocalDateTime.now());

            this.update(uw);
        });
    }

    @Override
    public void markFailRetry(Long id, int retryCount, LocalDateTime nextRetryTime, String lastError) {
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        redissonLockExecutor.execute(lock, () -> {
            // 更新
            LambdaUpdateWrapper<ShopWebhookEvent> uw = new LambdaUpdateWrapper<>();
            uw.eq(ShopWebhookEvent::getId, id);

            uw.set(ShopWebhookEvent::getStatus, ShopWebhookStatus.FAILED_RETRY.getValue());
            uw.set(ShopWebhookEvent::getRetryCount, retryCount);
            uw.set(ShopWebhookEvent::getNextRetryTime, nextRetryTime);
            uw.set(ShopWebhookEvent::getLastError, safe(lastError));
            uw.set(ShopWebhookEvent::getUpdateTime, LocalDateTime.now());

            this.update(uw);
        });
    }

    @Override
    public void markAbandoned(Long id, String lastError) {
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        redissonLockExecutor.execute(lock, () -> {
            // 更新
            LambdaUpdateWrapper<ShopWebhookEvent> uw = new LambdaUpdateWrapper<>();
            uw.eq(ShopWebhookEvent::getId, id);

            uw.set(ShopWebhookEvent::getStatus, ShopWebhookStatus.ABANDONED.getValue());
            uw.set(ShopWebhookEvent::getNextRetryTime, null);
            uw.set(ShopWebhookEvent::getLastError, safe(lastError));
            uw.set(ShopWebhookEvent::getUpdateTime, LocalDateTime.now());

            this.update(uw);
        });
    }

    @Override
    public RespPage<AdminShopWebhookEventVo> getAdminShopWebhookEventVoPage(AdminShopWebhookEventPageQuery query) {
        IPage<AdminShopWebhookEventVo> page = baseMapper.selectAdminShopWebhookEventVoPage(new Page<>(query.getPageNum(), query.getPageSize()), query);
        return RespPageConvert.convert(page, AdminShopWebhookEventVo.class);
    }

    @Override
    public void retry(Long... ids) {
        if (ids == null || ids.length == 0) {
            return;
        }

        // 去重 + 去 null
        List<Long> idList = Arrays.stream(ids)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (idList.isEmpty()) {
            return;
        }

        // 并发上限 (每批并发数量)
        int maxConcurrency = Math.min(32, idList.size());
        Semaphore semaphore = new Semaphore(maxConcurrency);

        // 失败收集
        Queue<Long> failedIds = new ConcurrentLinkedQueue<>();
        Queue<Throwable> causes = new ConcurrentLinkedQueue<>();

        // 批量并发完成
        List<CompletableFuture<Void>> futures = idList.stream()
                .map(id -> CompletableFuture.runAsync(() -> {
                    boolean acquired = false;
                    try {
                        semaphore.acquire();
                        acquired = true;
                        retryOne(id);
                    } catch (Throwable e) {
                        failedIds.add(id);
                        causes.add(e);
                    } finally {
                        if (acquired) {
                            semaphore.release();
                        }
                    }
                }, taskExecutor))
                .toList();

        // 阻塞等待完成
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        if (!failedIds.isEmpty()) {
            Throwable first = causes.peek();
            throw new BizException("回调事件批量重试失败, failedIds=" + failedIds, first);
        }
    }

    @Override
    public void retryAllFail(Long shopId) {
        // 查询商户所有回调失败的ID
        List<Long> idList = baseMapper.selectShopAllFailIdList(shopId);
        if (idList.isEmpty()) {
            return;
        }
        // 批量并发修改
        retry(idList.toArray(Long[]::new));
    }

    @Override
    public ShopWebhookEvent getById(Long id) {
        return baseMapper.selectById(id);
    }

    private void retryOne(Long id) {
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        redissonLockExecutor.execute(lock, () -> {
            LambdaUpdateWrapper<ShopWebhookEvent> uw = Wrappers.lambdaUpdate();
            uw.eq(ShopWebhookEvent::getId, id)
                    .set(ShopWebhookEvent::getStatus, ShopWebhookStatus.PENDING.getValue())
                    .set(ShopWebhookEvent::getRetryCount, 0)
                    .set(ShopWebhookEvent::getNextRetryTime, LocalDateTime.now())
                    .set(ShopWebhookEvent::getUpdateTime, LocalDateTime.now());

            int rows = baseMapper.update(null, uw);
            if (rows != 1) {
                throw new BizException("回调事件重试失败, id=" + id + ", rows=" + rows);
            }
        });
    }

    private String safe(String s) {
        if (!StringUtils.hasText(s)) return s;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }
}
