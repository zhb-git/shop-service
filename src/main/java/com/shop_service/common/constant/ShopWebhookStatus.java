package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商户回调状态
 *
 * @author 啊祖
 * @date 2026-01-14 19:16
 **/
@AllArgsConstructor
@Getter
public enum ShopWebhookStatus {
    PENDING(0, "待发送"),
    SENDING(1, "发送中"),
    SUCCESS(2, "发送成功"),
    FAILED_RETRY(3, "发送失败(待重试)"),
    ABANDONED(4, "终止/放弃");

    private final int value;
    private final String description;
}
