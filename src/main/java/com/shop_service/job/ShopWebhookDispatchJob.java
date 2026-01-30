package com.shop_service.job;

import com.shop_service.common.core.RedissonLockExecutor;
import com.shop_service.model.pojo.DueEvent;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.service.IShopService;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    private IShopWebhookEventService shopWebhookEventService;

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
            List<DueEvent> due = shopWebhookEventService.getDueEventList(now, BATCH_SIZE);
            if (due == null || due.isEmpty()) {
                return;
            }

            // 虚拟线程无需大线程池, 但仍建议本批次 join, 防止下一轮叠加过多任务
            CompletableFuture<?>[] futures = new CompletableFuture[due.size()];
            AtomicInteger idx = new AtomicInteger(0);

            for (DueEvent event : due) {
                // 查询商户信息
                ShopInfo shopInfo = shopService.getShopInfoById(event.getShopId());
                // 异步执行
                futures[idx.getAndIncrement()] = CompletableFuture.runAsync(() -> shopWebhookEventService.sendWebhook(shopInfo, event.getId()), taskExecutor);
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
}
