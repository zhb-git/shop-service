package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 开卡订单
 *
 * @author 啊祖
 * @date 2026-01-15 14:24
 **/
@Data
public class OpenCardOrder {
    /**
     * 商户信息
     */
    private ShopInfo shopInfo;

    /**
     * 卡头ID
     */
    private Long cardBinId;

    /**
     * 花费金额
     */
    private BigDecimal spendAmount;
}
