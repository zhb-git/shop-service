package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡片状态
 *
 * @author 啊祖
 * @date 2026-01-11 03:41
 **/
@AllArgsConstructor
@Getter
public enum VsCardStatus {
    NORMAL(0, "正常"),
    FROZEN(1, "已冻结"),
    CANCELLED(2, "已注销");

    private final int value;
    private final String description;

    /**
     * 是否允许进行交易
     */
    public boolean canPay() {
        return this == NORMAL;
    }

    /**
     * 是否允许解冻 (只有冻结状态才能解冻)
     */
    public boolean canUnfreeze() {
        return this == FROZEN;
    }

    /**
     * 是否为终态 (注销后不可逆)
     */
    public boolean isFinalStatus() {
        return this == CANCELLED;
    }


    /**
     * 根据状态获取枚举
     * @param value 状态
     * @return 枚举
     */
    public static VsCardStatus fromValue(int value) {
        return Arrays.stream(VsCardStatus.values())
                .filter(type -> type.value == value)
                .findFirst()
                .orElse(null);
    }
}
