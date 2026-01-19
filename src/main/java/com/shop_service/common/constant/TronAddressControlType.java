package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 波场地址监控类型
 *
 * @author 啊祖
 * @date 2026-01-14 20:00
 **/
@AllArgsConstructor
@Getter
public enum TronAddressControlType {
    TRANSFER(1, "转账");

    private final int value;
    private final String description;
}
