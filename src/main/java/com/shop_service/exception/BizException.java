package com.shop_service.exception;

/**
 * 业务异常
 *
 * @author 啊祖
 * @date 2026-01-12 09:47
 **/
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }
}
