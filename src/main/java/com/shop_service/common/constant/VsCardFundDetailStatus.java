package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡片资金明细状态
 *
 * @author 啊祖
 * @date 2026-01-16 16:29
 **/
@AllArgsConstructor
@Getter
public enum VsCardFundDetailStatus {
    CLOSED("Closed", "已完成"),
    PENDING("Pending", "处理中"),
    FAIL("Fail", "失败");

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
     * @param value 状态值
     * @return 枚举
     */
    public static VsCardFundDetailStatus fromValue(String value) {
        if (value == null) {
            return null;
        }
        return Arrays.stream(values())
                .filter(e -> e.value.equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}
