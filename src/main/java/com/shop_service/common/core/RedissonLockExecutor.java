package com.shop_service.common.core;

import com.shop_service.exception.LockAcquireException;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * redis锁
 * 设计原则:
 * 1. 默认开启 watchdog 自动续期, 避免任务执行超过 lease 导致锁提前释放
 * 2. 单锁和多锁提供一致的语义, 都支持 blocking 和 tryLock
 * 3. 多锁使用 RMultiLock, 降低死锁与回滚复杂度
 * 4. 所有 key 自动加前缀, 避免不同业务锁名冲突
 *
 * @author 啊祖
 * @date 2026-01-12 14:49
 **/
@Slf4j
@Component
public class RedissonLockExecutor {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 默认等待时间(秒), 仅用于 tryLock
     */
    private static final long DEFAULT_WAIT_SECONDS = 5;

    /**
     * 默认租期(秒), 仅用于显式 lease 的场景
     * 注意: 传 lease 会关闭 watchdog, 任务必须在租期内完成
     */
    private static final long DEFAULT_LEASE_SECONDS = 30;

    /**
     * 锁 key 前缀, 建议按业务自定义
     */
    private static final String LOCK_PREFIX = "lock:wallet:";

    /**
     * 单锁执行, 默认 tryLock(waitSeconds), 使用 watchdog 自动续期
     */
    public void execute(String key, Runnable task) {
        execute(key, DEFAULT_WAIT_SECONDS, () -> {
            task.run();
            return null;
        });
    }

    /**
     * 单锁执行, 默认 tryLock(waitSeconds), 使用 watchdog 自动续期
     */
    public <T> T execute(String key, Supplier<T> task) {
        return execute(key, DEFAULT_WAIT_SECONDS, task);
    }

    /**
     * 单锁执行, tryLock(waitSeconds), 使用 watchdog 自动续期
     * 说明: 这里不传 lease, 保留 watchdog
     */
    public <T> T execute(String key, long waitSeconds, Supplier<T> task) {
        RLock lock = getLock(key);

        boolean locked = false;
        try {
            locked = lock.tryLock(waitSeconds, TimeUnit.SECONDS);
            if (!locked) {
                throw new LockAcquireException("获取锁失败", lock.getName(), waitSeconds, null, false, null);
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquireException("线程中断", lock.getName(), waitSeconds, null, false, e);
        } finally {
            unlockQuietly(lock, locked);
        }
    }

    /**
     * 单锁执行, 显式租期版本
     * 注意: 传 lease 会关闭 watchdog, 任务必须在租期内完成
     */
    public <T> T executeWithLease(String key, long waitSeconds, long leaseSeconds, Supplier<T> task) {
        RLock lock = getLock(key);

        boolean locked = false;
        try {
            locked = lock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
            if (!locked) {
                throw new LockAcquireException("获取锁失败", lock.getName(), waitSeconds, leaseSeconds, false, null);
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquireException("线程中断", lock.getName(), waitSeconds, leaseSeconds, false, e);
        } finally {
            unlockQuietly(lock, locked);
        }
    }

    /**
     * 多锁执行(阻塞), 默认 watchdog 自动续期
     */
    public void execute(Set<String> keys, Runnable task) {
        execute(keys, () -> {
            task.run();
            return null;
        });
    }

    /**
     * 多锁执行(阻塞), 默认 watchdog 自动续期
     */
    public <T> T execute(Set<String> keys, Supplier<T> task) {
        return doExecuteMulti(keys, false, DEFAULT_WAIT_SECONDS, null, task);
    }

    /**
     * 多锁 tryLock 版本, 默认等待 DEFAULT_WAIT_SECONDS, watchdog 自动续期
     */
    public void tryExecute(Set<String> keys, Runnable task) {
        tryExecute(keys, DEFAULT_WAIT_SECONDS, () -> {
            task.run();
            return null;
        });
    }

    /**
     * 多锁 tryLock 版本, 默认等待 DEFAULT_WAIT_SECONDS, watchdog 自动续期
     */
    public <T> T tryExecute(Set<String> keys, Supplier<T> task) {
        return tryExecute(keys, DEFAULT_WAIT_SECONDS, task);
    }

    /**
     * 多锁 tryLock 版本, 自定义等待时间, watchdog 自动续期
     */
    public <T> T tryExecute(Set<String> keys, long waitSeconds, Supplier<T> task) {
        return doExecuteMulti(keys, true, waitSeconds, null, task);
    }

    /**
     * 多锁显式租期版本(阻塞或 tryLock 均可)
     * 注意: 传 lease 会关闭 watchdog, 任务必须在租期内完成
     */
    public <T> T executeWithLease(Set<String> keys, boolean tryLock, long waitSeconds, long leaseSeconds, Supplier<T> task) {
        return doExecuteMulti(keys, tryLock, waitSeconds, leaseSeconds, task);
    }

    private <T> T doExecuteMulti(Set<String> keys, boolean tryLock, long waitSeconds, Long leaseSeconds, Supplier<T> task) {
        if (keys == null || keys.isEmpty()) {
            return task.get();
        }

        // 排序用于稳定锁顺序, 即使使用 MultiLock, 也便于排查与一致性
        List<String> sorted = keys.stream()
                .filter(StringUtils::hasText)
                .map(this::normalizeKey)
                .distinct()
                .sorted()
                .toList();

        if (sorted.isEmpty()) {
            return task.get();
        }

        RLock[] lockArr = sorted.stream().map(redissonClient::getLock).toArray(RLock[]::new);
        RLock multiLock = redissonClient.getMultiLock(lockArr);

        boolean locked = false;
        try {
            if (tryLock) {
                if (leaseSeconds == null) {
                    // watchdog
                    locked = multiLock.tryLock(waitSeconds, TimeUnit.SECONDS);
                } else {
                    // no watchdog
                    locked = multiLock.tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
                }
            } else {
                if (leaseSeconds == null) {
                    // watchdog
                    multiLock.lock();
                } else {
                    // no watchdog
                    multiLock.lock(leaseSeconds, TimeUnit.SECONDS);
                }
                locked = true;
            }

            if (!locked) {
                throw new LockAcquireException(
                        "获取锁失败",
                        String.join(",", sorted),
                        waitSeconds,
                        leaseSeconds,
                        true,
                        null
                );
            }

            return task.get();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquireException(
                    "线程中断",
                    String.join(",", sorted),
                    waitSeconds,
                    leaseSeconds,
                    true,
                    e
            );
        } finally {
            // multiLock 解锁会逐个释放内部锁
            unlockQuietly(multiLock, locked);
        }
    }

    private RLock getLock(String key) {
        String lockKey = normalizeKey(key);
        return redissonClient.getLock(lockKey);
    }

    private String normalizeKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new IllegalArgumentException("lock key is blank");
        }
        // 避免重复前缀
        if (key.startsWith(LOCK_PREFIX)) {
            return key;
        }
        return LOCK_PREFIX + key;
    }

    private void unlockQuietly(RLock lock, boolean locked) {
        if (!locked || lock == null) {
            return;
        }
        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        } catch (Exception e) {
            log.warn("释放锁失败: {}", safeName(lock), e);
        }
    }

    private String safeName(RLock lock) {
        try {
            return lock.getName();
        } catch (Exception ignored) {
            return "unknown-lock";
        }
    }
}
