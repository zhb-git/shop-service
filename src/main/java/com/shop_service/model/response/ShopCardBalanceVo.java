package com.shop_service.model.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 卡片余额
 *
 * @author 啊祖
 * @date 2026-01-15 16:05
 **/
@Data
public class ShopCardBalanceVo {
    /**
     * 可用余额
     */
    private BigDecimal available;

    /**
     * 币种
     */
    private String currency;

    /**
     * 冻结中的金额
     */
    private BigDecimal frozen;

    /**
     * 处理中的金额
     */
    private BigDecimal pending;
}
