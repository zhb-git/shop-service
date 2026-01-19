package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户账户余额存入回调数据
 * <p>
 * 向商户专属的USDT-TRC20地址充值成功后回调
 * @author 啊祖
 * @date 2026-01-14 20:19
 **/
@Data
public class ShopAssetDepositHookPayload {
    /**
     * 存入金额
     */
    private BigDecimal amount;

    /**
     * 存入方式
     */
    private Integer manner;

    /**
     * 存入凭据
     */
    private String credential;
}
