package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 波场链币种
 *
 * @author 啊祖
 * @date 2026-01-14 19:49
 **/
@AllArgsConstructor
@Getter
public enum TronChainCoin {
    TRX("TRX", "TRX币种"),
    USDT("USDT", "USDT币种");

    private final String value;
    private final String description;
}
