package com.shop_service.exception;

/**
 * 参数异常
 *
 * @author 啊祖
 * @date 2026-01-12 08:41
 **/
public class QueryException extends RuntimeException {
    public QueryException(String message) {
        super(message);
    }

    public QueryException(String message, Throwable cause) {
        super(message, cause);
    }
}
