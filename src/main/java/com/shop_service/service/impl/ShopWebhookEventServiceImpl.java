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
import com.shop_service.common.core.ShopWebhookSender;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopWebhookEventMapper;
import com.shop_service.model.entity.ShopWebhookEvent;
import com.shop_service.model.pojo.DueEvent;
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
    // 最大重试次数
    private static final int MAX_RETRY = 10;

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

        // 是否发送
        boolean isSend = Boolean.TRUE.equals(shopInfo.getEnabled())
                && Boolean.TRUE.equals(shopInfo.getWebhookEnabled())
                && shopInfo.getWebhookUrl() != null;

        if (isSend) {
            event.setStatus(ShopWebhookStatus.PENDING.getValue());
            event.setRetryCount(0);
            event.setNextRetryTime(LocalDateTime.now());
        } else {
            event.setStatus(ShopWebhookStatus.ABANDONED.getValue());
            event.setRetryCount(0);
            event.setLastError("商户被停用, 或者商户未开启回调, 或者设置未设置回调url");
        }

        if (baseMapper.insert(event) != 1) {
            throw new BizException("商户回调时间新增失败");
        }

        if (isSend) {
            // 发送回调 (首发)
            sendWebhook(shopInfo, event.getId(), true);
        }
    }

    @Override
    public List<DueEvent> getDueEventList(LocalDateTime now, int limit) {
        return baseMapper.selectDueEventList(
                now,
                limit,
                ShopWebhookStatus.PENDING.getValue(),
                ShopWebhookStatus.FAILED_RETRY.getValue()
        );
    }

    @Override
    public void sendWebhook(ShopInfo shopInfo, Long id, boolean isFirst) {
        // 加锁执行
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        redissonLockExecutor.execute(lock, () -> {
            // 查询事件
            ShopWebhookEvent event = getById(id);

            // 非首发则校验条件
            if (!isFirst) {
                // 校验状态, 防止重复发, 只发状态: 待发送 or 发送失败(待重试)
                if (ShopWebhookStatus.PENDING.getValue() != event.getStatus() &&
                        ShopWebhookStatus.FAILED_RETRY.getValue() != event.getStatus()) {
                    return;
                }
            }

            LocalDateTime nowTime = LocalDateTime.now();

            try {
                // 领取任务: 仅允许 PENDING/FAILED_RETRY 且到期的记录标记为 SENDING
                boolean picked = markSending(event.getId(), nowTime, isFirst);
                if (!picked) {
                    return;
                }

                // 基本校验
                if (!Boolean.TRUE.equals(shopInfo.getEnabled())) {
                    markAbandoned(event.getId(), "商户已被停用");
                    return;
                }
                if (!Boolean.TRUE.equals(shopInfo.getWebhookEnabled())) {
                    markAbandoned(event.getId(), "商户未开启回调");
                    return;
                }
                if (!StringUtils.hasText(shopInfo.getWebhookSecret())) {
                    markAbandoned(event.getId(), "商户未配置回调签名密钥");
                    return;
                }

                // webhookUrl: 优先使用事件快照, 为空则兜底使用商户当前配置
                String webhookUrl = StringUtils.hasText(event.getWebhookUrl()) ? event.getWebhookUrl() : shopInfo.getWebhookUrl();
                if (!StringUtils.hasText(webhookUrl)) {
                    markAbandoned(event.getId(), "商户未配置回调地址");
                    return;
                }
                event.setWebhookUrl(webhookUrl);

                // 发送回调
                ShopWebhookSender.SendResult result = ShopWebhookSender.execute(shopInfo, event);
                log.info("商户[{} / {}]回调事件 - 系统事件ID={}, 回调响应: {} / {}", shopInfo.getNo(), shopInfo.getName(), id, result.httpCode(), result.message());

                // 更新状态
                if (result.ok()) {
                    markSuccess(event.getId());
                    return;
                }

                int retry = (event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1;
                if (retry >= MAX_RETRY) {
                    markAbandoned(event.getId(), "回调失败次数已达上限, HTTP状态码=" + result.httpCode() + ", 响应=" + safe(result.message()));
                    return;
                }

                LocalDateTime nextTime = nowTime.plusSeconds(calcDelaySeconds(retry));
                markFailRetry(event.getId(), retry, nextTime, "回调失败, HTTP状态码=" + result.httpCode() + ", 响应=" + safe(result.message()));

            } catch (Exception ex) {
                log.warn("商户回调任务异常, 事件数据库ID={}, eventId={}, 异常={}", event.getId(), event.getEventId(), ex.toString());

                try {
                    int retry = (event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1;
                    String reason = "回调发送异常: " + ex.getClass().getSimpleName() + ", " + safe(ex.getMessage());

                    if (retry >= MAX_RETRY) {
                        markAbandoned(event.getId(), reason);
                    } else {
                        LocalDateTime next = LocalDateTime.now().plusSeconds(calcDelaySeconds(retry));
                        markFailRetry(event.getId(), retry, next, reason);
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }
        });
        log.info("商户[{} / {}]回调事件 - 系统事件ID={}, 执行完成", shopInfo.getNo(), shopInfo.getName(), id);
    }

    /**
     * 简单退避(不复杂但实用)
     * 1->5s, 2->10s, 3->30s, 4->60s, 5->120s, 其它->300s
     */
    private int calcDelaySeconds(int retry) {
        return switch (retry) {
            case 1 -> 5;
            case 2 -> 10;
            case 3 -> 30;
            case 4 -> 60;
            case 5 -> 120;
            default -> 300;
        };
    }

    /**
     * 领取一个回调事件并标记为发送中(SENDING)
     * 说明:
     * - 这是一个"条件更新"(CAS)操作, 用于避免同一条事件被重复发送
     * - 只有当事件当前状态为 PENDING/FAILED_RETRY 且已到期时, 才会更新为 SENDING
     * - 领取成功返回 true, 否则返回 false(表示已被其他线程/任务领取或不满足条件)
     *
     * @param id      事件表主键
     * @param now     当前时间
     * @param isFirst 是否首发
     * @return 是否领取成功
     */
    private boolean markSending(Long id, LocalDateTime now, boolean isFirst) {
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        return redissonLockExecutor.execute(lock, () -> {
            // 更新状态 && 最后发送时间
            LambdaUpdateWrapper<ShopWebhookEvent> uw = new LambdaUpdateWrapper<>();
            uw.eq(ShopWebhookEvent::getId, id);
            // 非首发则校验附加条件
            if (!isFirst) {
                uw.in(ShopWebhookEvent::getStatus,
                        ShopWebhookStatus.PENDING.getValue(),
                        ShopWebhookStatus.FAILED_RETRY.getValue()
                );
                uw.and(w -> w.isNull(ShopWebhookEvent::getNextRetryTime).or().le(ShopWebhookEvent::getNextRetryTime, now));
            }

            // 更新状态
            uw.set(ShopWebhookEvent::getStatus, ShopWebhookStatus.SENDING.getValue());
            uw.set(ShopWebhookEvent::getLastSendTime, now);
            uw.set(ShopWebhookEvent::getUpdateTime, LocalDateTime.now());

            return this.update(uw);
        });
    }

    /**
     * 标记回调事件发送成功(SUCCESS)
     * 说明:
     * - status -> SUCCESS
     * - nextRetryTime -> null
     * - lastError -> null
     *
     * @param id 事件表主键
     */
    private void markSuccess(Long id) {
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

    /**
     * 标记回调事件发送失败(待重试)(FAILED_RETRY)
     * 说明:
     * - status -> FAILED_RETRY
     * - retryCount -> 递增后的重试次数
     * - nextRetryTime -> 下次重试时间(由调度策略计算)
     * - lastError -> 记录失败原因(建议截断避免过长)
     *
     * @param id            事件表主键
     * @param retryCount    已重试次数(递增后)
     * @param nextRetryTime 下次重试时间
     * @param lastError     失败原因(简短)
     */
    private void markFailRetry(Long id, int retryCount, LocalDateTime nextRetryTime, String lastError) {
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

    /**
     * 标记回调事件终止/放弃(ABANDONED)
     * 适用场景:
     * - 商户停用
     * - 商户关闭回调
     * - 缺少 callbackUrl / callbackSecret 等关键配置
     * - 超过最大重试次数(可在此处终止)
     *
     * @param id        事件表主键
     * @param lastError 放弃原因(简短)
     */
    private void markAbandoned(Long id, String lastError) {
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

    private void retryOne(Long id) {
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        redissonLockExecutor.execute(lock, () -> {
            LambdaUpdateWrapper<ShopWebhookEvent> uw = Wrappers.lambdaUpdate();
            uw.eq(ShopWebhookEvent::getId, id)
                    // 重置属性
                    .set(ShopWebhookEvent::getStatus, ShopWebhookStatus.PENDING.getValue())
                    .set(ShopWebhookEvent::getRetryCount, 0)
                    .set(ShopWebhookEvent::getNextRetryTime, LocalDateTime.now())
                    .set(ShopWebhookEvent::getLastSendTime, null)
                    .set(ShopWebhookEvent::getLastError, null)
                    .set(ShopWebhookEvent::getUpdateTime, LocalDateTime.now());

            int rows = baseMapper.update(uw);
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
