package com.shop_service.exception;

/**
 * 认证异常
 *
 * @author 啊祖
 * @date 2026-01-14 18:35
 **/
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
