package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡片转账类型
 *
 * @author 啊祖
 * @date 2026-01-15 18:21
 **/
@AllArgsConstructor
@Getter
public enum VsCardTransferType {
    TRANSFER_IN(1, "转入"),
    TRANSFER_OUT(2, "转出");

    private final int value;
    private final String description;

    /**
     * 根据 value 获取枚举
     *
     * @param value 操作类型值
     * @return 枚举对象
     */
    public static VsCardTransferType fromValue(int value) {
        return Arrays.stream(VsCardTransferType.values())
                .filter(type -> type.value == value)
                .findFirst()
                .orElse(null);
    }
}
