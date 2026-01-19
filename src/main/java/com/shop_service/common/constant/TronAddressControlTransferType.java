package com.shop_service.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 波场监控地址转账类型
 *
 * @author 啊祖
 * @date 2026-01-14 19:58
 **/
@AllArgsConstructor
@Getter
public enum TronAddressControlTransferType {
    TRANSFER_IN(1, "转入"),
    TRANSFER_OUT(2, "转出");

    private final int value;
    private final String description;
}
