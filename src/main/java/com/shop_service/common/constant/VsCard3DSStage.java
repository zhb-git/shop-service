package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 卡片3DS验证阶段
 *
 * @author 啊祖
 * @date 2026-01-11 10:23
 **/
@AllArgsConstructor
@Getter
public enum VsCard3DSStage {
    OTP("OPT", "OPT验证"),
    FORWARDING("FORWARDING", "跳转验证");

    private final String value;
    private final String description;

    /**
     * 根据 value 获取枚举
     *
     * @param value 操作类型值
     * @return 枚举对象
     */
    public static VsCard3DSStage fromValue(String value) {
        return Arrays.stream(VsCard3DSStage.values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
