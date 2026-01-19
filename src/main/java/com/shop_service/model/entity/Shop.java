package com.shop_service.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户
 *
 * @author 啊祖
 * @date 2026-01-14 14:54
 **/
@Data
// 若有字段需要json映射得开启autoResultMap = true, 且标明@TableField(typeHandler = JacksonTypeHandler.class)注解
@TableName(value = "shop", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class Shop extends BaseEntity {
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
    @TableField(typeHandler = JacksonTypeHandler.class)
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
