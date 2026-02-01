package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 商户回调事件类型
 *
 * @author 啊祖
 * @date 2026-01-14 18:46
 **/
@AllArgsConstructor
@Getter
public enum ShopWebhookEventType {
    // 交易与账单
    CARD_TRANSFER_RESULT("CardTransferResult", "卡片转账结果"),
    CARD_FUND_DETAIL("CardFundDetail", "卡片资金明细"),
    CARD_OVERSPEND("CardOverspend", "卡片超支"),
    CARD_SETTLEMENT("CardSettlement", "卡片超支结算"),

    // 卡片相关
    OPEN_CARD_RESULT("OpenCardResult", "开卡结果"),
    DESTROY_CARD("DestroyCard", "卡片注销"),
    DESTROY_CARD_RESULT("DestroyCardResult", "卡片注销结果"),
    FROZEN_CARD("FrozenCard", "冻结卡片"),
    FROZEN_CARD_RESULT("FrozenCardResult", "冻结卡片结果"),
    UNFROZEN_CARD("UnfrozenCard", "解冻卡片"),
    UNFROZEN_CARD_RESULT("UnfrozenCardResult", "解冻卡片结果"),
    CARD_BIN_STATUS("CardBinStatus", "卡Bin状态变更"),
    CARD_BIND_PLATFORM("CardBindPlatform", "平台绑定卡片"),

    // 安全与验证
    CARD_3DS("Card3ds", "3DS验证"),

    // 商户相关
    ASSETS_DEPOSIT("AssetsDeposit", "商户余额存入");

    private final String value;
    private final String description;
}
