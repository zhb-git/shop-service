package com.shop_service.exception;

/**
 * 第三方接口请求异常
 *
 * @author 啊祖
 * @date 2026-01-14 17:48
 **/
public class ApiRequestException extends RuntimeException {
    public ApiRequestException(String message) {
        super(message);
    }

    public ApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
