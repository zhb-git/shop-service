package com.shop_service.job;

import com.shop_service.common.constant.LockServiceType;
import com.shop_service.common.constant.ShopWebhookStatus;
import com.shop_service.common.core.LockKeyProduce;
import com.shop_service.common.core.RedissonLockExecutor;
import com.shop_service.common.core.ShopWebhookSender;
import com.shop_service.model.entity.ShopWebhookEvent;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.service.IShopService;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 商户回调: 定时任务扫描发送
 *
 * @author 啊祖
 * @date 2026-01-14 19:10
 **/
@Slf4j
@Component
public class ShopWebhookDispatchJob {
    @Resource
    private RedissonLockExecutor redissonLockExecutor;

    @Resource
    private IShopWebhookEventService webhookEventService;

    @Resource
    private IShopService shopService;

    @Resource(name = "applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    /**
     * Job 级别锁, 防止定时任务重入
     */
    private static final String DISPATCH_LOCK_ID = "DispatchJob";

    /**
     * 每轮抓取数量
     */
    private static final int BATCH_SIZE = 300;

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY = 10;

    /**
     * 单次调度最多跑几轮, 避免一直占用调度线程
     */
    private static final int MAX_ROUND_PER_TICK = 5;

    /**
     * 固定延迟调度, 默认 1s 一次
     */
    @Scheduled(fixedDelay = 1000)
    public void job() {
        // waitSeconds=0, 拿不到锁就跳过(高并发下避免堆积)
        redissonLockExecutor.execute(DISPATCH_LOCK_ID, 0, () -> {
            doDispatch();
            return null;
        });
    }

    private void doDispatch() {
        for (int round = 0; round < MAX_ROUND_PER_TICK; round++) {
            // 查询待推送
            LocalDateTime now = LocalDateTime.now();
            List<Long> due = webhookEventService.getDueEventList(now, BATCH_SIZE);
            if (due == null || due.isEmpty()) {
                return;
            }

            // 虚拟线程无需大线程池, 但仍建议本批次 join, 防止下一轮叠加过多任务
            CompletableFuture<?>[] futures = new CompletableFuture[due.size()];
            AtomicInteger idx = new AtomicInteger(0);

            for (Long id : due) {
                futures[idx.getAndIncrement()] = CompletableFuture.runAsync(() -> handleOne(id), taskExecutor);
            }

            // 等待本批任务完成
            try {
                CompletableFuture.allOf(futures).join();
            } catch (Exception ignored) {
                // 单个任务异常在 handleOne 内部兜底
            }

            if (due.size() < BATCH_SIZE) {
                return;
            }
        }
    }

    private void handleOne(Long id) {
        // 加锁执行
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_WEBHOOK_EVENT, id);
        redissonLockExecutor.execute(lock, () -> {
            // 查询事件
            ShopWebhookEvent event = webhookEventService.getById(id);

            // 校验状态
            if (ShopWebhookStatus.PENDING.getValue() != event.getStatus() &&
                    ShopWebhookStatus.FAILED_RETRY.getValue() != event.getStatus()) {
                return;
            }

            LocalDateTime now = LocalDateTime.now();

            try {
                // 1. 领取任务: 仅允许 PENDING/FAILED_RETRY 且到期的记录标记为 SENDING
                boolean picked = webhookEventService.markSending(event.getId(), now);
                if (!picked) {
                    return;
                }

                // 2. 从缓存读取商户信息(高性能)
                ShopInfo shopInfo = shopService.getShopInfoByNo(event.getShopNo());
                if (shopInfo == null) {
                    webhookEventService.markAbandoned(event.getId(), "商户不存在或缓存已失效");
                    return;
                }

                // 3. 基本校验
                if (!Boolean.TRUE.equals(shopInfo.getEnabled())) {
                    webhookEventService.markAbandoned(event.getId(), "商户已被停用");
                    return;
                }
                if (!Boolean.TRUE.equals(shopInfo.getWebhookEnabled())) {
                    webhookEventService.markAbandoned(event.getId(), "商户未开启回调");
                    return;
                }
                if (!StringUtils.hasText(shopInfo.getWebhookSecret())) {
                    webhookEventService.markAbandoned(event.getId(), "商户未配置回调签名密钥");
                    return;
                }

                // webhookUrl: 优先使用事件快照, 为空则兜底使用商户当前配置
                String webhookUrl = StringUtils.hasText(event.getWebhookUrl()) ? event.getWebhookUrl() : shopInfo.getWebhookUrl();
                if (!StringUtils.hasText(webhookUrl)) {
                    webhookEventService.markAbandoned(event.getId(), "商户未配置回调地址");
                    return;
                }
                event.setWebhookUrl(webhookUrl);

                // 4. 发送回调
                ShopWebhookSender.SendResult result = ShopWebhookSender.send(shopInfo, event);

                // 5. 更新状态
                if (result.ok()) {
                    webhookEventService.markSuccess(event.getId());
                    return;
                }

                int retry = (event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1;
                if (retry >= MAX_RETRY) {
                    webhookEventService.markAbandoned(event.getId(), "回调失败次数已达上限, httpCode=" + result.httpCode() + ", 响应=" + safe(result.message()));
                    return;
                }

                LocalDateTime next = now.plusSeconds(calcDelaySeconds(retry));
                webhookEventService.markFailRetry(event.getId(), retry, next, "回调失败, httpCode=" + result.httpCode() + ", 响应=" + safe(result.message()));

            } catch (Exception ex) {
                log.warn("商户回调任务异常, 事件数据库ID={}, eventId={}, 异常={}", event.getId(), event.getEventId(), ex.toString());

                try {
                    int retry = (event.getRetryCount() == null ? 0 : event.getRetryCount()) + 1;
                    String reason = "回调发送异常: " + ex.getClass().getSimpleName() + ", " + safe(ex.getMessage());

                    if (retry >= MAX_RETRY) {
                        webhookEventService.markAbandoned(event.getId(), reason);
                    } else {
                        LocalDateTime next = LocalDateTime.now().plusSeconds(calcDelaySeconds(retry));
                        webhookEventService.markFailRetry(event.getId(), retry, next, reason);
                    }
                } catch (Exception ignored) {
                    // ignore
                }
            }
        });
    }

    private String safe(String s) {
        if (!StringUtils.hasText(s)) return s;
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    /**
     * 简单退避(不复杂但实用)
     * 1->5s, 2->10s, 3->30s, 4->60s, 5->120s, 其他->300s
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
}
