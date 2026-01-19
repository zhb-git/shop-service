package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡片资金明细类型
 *
 * @author 啊祖
 * @date 2026-01-11 08:50
 **/
@AllArgsConstructor
@Getter
public enum VsCardFundDetailType {
    CONSUMPTION("Consumption", "消费"),
    TRANSFER_IN("TransferIn", "转入"),
    TRANSFER_OUT("TransferOut", "转出"),
    FROZEN("Frozen", "冻结卡片金额"),
    UN_FROZEN("UnFrozen", "解冻卡片金额"),
    REVERSAL("Reversal", "撤销"),
    CREDIT("Credit", "退款"),
    FEE_CONSUMPTION("Fee_Consumption", "授权");

    /**
     * 接口 / 数据库存储值
     */
    private final String value;

    /**
     * 中文描述
     */
    private final String description;

    /**
     * 根据 value 获取枚举
     *
     * @param value 接口返回的类型
     * @return 枚举
     */
    public static VsCardFundDetailType fromValue(String value) {
        return Arrays.stream(values())
                .filter(e -> e.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
