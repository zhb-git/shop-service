package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 锁的业务类型
 *
 * @author 啊祖
 * @date 2026-01-12 14:57
 **/
@AllArgsConstructor
@Getter
public enum LockServiceType {
    SHOP("Shop", "商户锁, 系统商户ID"),
    SHOP_WEBHOOK_EVENT("ShopWebhookEvent", "商户回调事件锁, 系统事件ID"),
    VS_CALLBACK("VsCallback", "vs回调锁, 回调ID"),
    SHOP_CARD("ShopCard", "商户卡片锁, 系统卡片ID"),
    SHOP_CARD_BIN("ShopCardBin", "商户卡头, 系统商户ID + 卡头ID");

    private final String value;
    private final String description;
}
