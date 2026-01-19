package com.shop_service.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户信息
 *
 * @author 啊祖
 * @date 2026-01-14 17:18
 **/
@Data
public class AdminShopVo {
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
     * 商户余额
     */
    private BigDecimal balance;

    /**
     * 请求IP白名单
     */
    private List<String> ipWhitelist;

    /**
     * 回调签名密钥
     */
    private String webhookUrlSecret;

    /**
     * 回调url
     */
    private String webhookUrlUrl;

    /**
     * 回调超时 (毫秒)
     */
    private Integer webhookUrlTimeoutMs;

    /**
     * 是否启用回调
     */
    private Boolean webhookUrlEnabled;

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

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
