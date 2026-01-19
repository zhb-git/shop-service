package com.shop_service.model.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * vs接口响应结果
 *
 * @author 啊祖
 * @date 2026-01-14 22:56
 **/
@Data
public class VsApiResult<T> implements Serializable {
    /**
     * 响应码
     */
    private Integer code;

    /**
     * 消息
     */
    private String message;

    /**
     * 数据
     */
    private T data;

    public boolean isSuccess() {
        return code != null && code == 200;
    }
}
