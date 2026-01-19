package com.shop_service.exception;

import lombok.Getter;

/**
 * 获取锁失败
 *
 * @author 啊祖
 * @date 2026-01-12 14:52
 **/
@Getter
public class LockAcquireException extends RuntimeException {
    private final String lockName;
    private final Long waitSeconds;
    private final Long leaseSeconds;
    private final boolean multi;

    public LockAcquireException(String message, String lockName, Long waitSeconds, Long leaseSeconds, boolean multi, Throwable cause) {
        super(buildMessage(message, lockName, waitSeconds, leaseSeconds, multi), cause);
        this.lockName = lockName;
        this.waitSeconds = waitSeconds;
        this.leaseSeconds = leaseSeconds;
        this.multi = multi;
    }

    private static String buildMessage(String message, String lockName, Long waitSeconds, Long leaseSeconds, boolean multi) {
        return message
                + ", lock=" + lockName
                + ", waitSeconds=" + waitSeconds
                + ", leaseSeconds=" + leaseSeconds
                + ", multi=" + multi;
    }
}
