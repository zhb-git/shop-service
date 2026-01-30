package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 需要推送的事件
 *
 * @author 啊祖
 * @date 2026-01-21 12:43
 **/
@Data
public class DueEvent {
    /**
     * 系统事件ID
     */
    private Long id;

    /**
     * 商户ID
     */
    private Long shopId;
}
