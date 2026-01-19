package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户信息
 *
 * @author 啊祖
 * @date 2026-01-14 17:10
 **/
@Data
public class ShopInfo {
    /**
     * 商户ID
     */
    private Long id;

    /**
     * 商户号
     */
    private String no;

    /**
     * 商户名字
     */
    private String name;

    /**
     * 商户公钥 (请求)
     */
    private String publicKey;

    /**
     * 请求IP白名单
     */
    private List<String> ipWhitelist;

    /**
     * 回调签名密钥
     */
    private String webhookSecret;

    /**
     * 回调url
     */
    private String webhookUrl;

    /**
     * 回调超时 (毫秒)
     */
    private Integer webhookTimeoutMs;

    /**
     * 是否启用回调
     */
    private Boolean webhookEnabled;

    /**
     * 波场充值地址
     */
    private String tronRechargeAddress;

    /**
     * 波场充值地址私钥
     */
    private String tronRechargeAddressPrivateKey;

    /**
     * 充值费率
     */
    private BigDecimal rechargeFee;

    /**
     * 是否启用
     */
    private Boolean enabled;
}
