package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商户充值方式
 *
 * @author 啊祖
 * @date 2026-01-14 20:14
 **/
@AllArgsConstructor
@Getter
public enum ShopRechargeManner {
    USDT_TRC20(1, "USDT-TRC20充值"),
    ADMIN(2, "管理员操作");

    private final int value;
    private final String description;
}
