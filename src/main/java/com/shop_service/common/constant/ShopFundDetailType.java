package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商户资金明细类型
 *
 * @author 啊祖
 * @date 2026-01-14 20:07
 **/
@AllArgsConstructor
@Getter
public enum ShopFundDetailType {
    RECHARGE(1, "充值"),
    OPEN_CARD_SUB(2, "开卡扣款"),
    CARD_TRANSFER_IN(3, "卡片转入"),
    CARD_TRANSFER_OUT(4, "卡片转出"),
    OPEN_CARD_FAIL_BACK(5, "开卡失败退款"),
    ADMIN_TRANSFER_IN(6, "管理员转入"),
    ADMIN_TRANSFER_OUT(7, "管理员转出");

    private final int value;
    private final String description;
}
