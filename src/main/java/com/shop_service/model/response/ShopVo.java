package com.shop_service.model.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户信息
 *
 * @author 啊祖
 * @date 2026-01-14 18:32
 **/
@Data
public class ShopVo {
    /**
     * 商户余额
     */
    private BigDecimal balance;

    /**
     * 波场充值地址
     */
    private String tronRechargeAddress;
}
