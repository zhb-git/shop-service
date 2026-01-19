package com.shop_service.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户卡头
 *
 * @author 啊祖
 * @date 2026-01-14 14:56
 **/
@Data
// 若有字段需要json映射得开启autoResultMap = true, 且标明@TableField(typeHandler = JacksonTypeHandler.class)注解
@TableName(value = "shop_card_bin", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ShopCardBin extends BaseEntity {
    /**
     * 商户ID
     */
    private Long shopId;

    /**
     * 商户号
     */
    private String shopNo;

    /**
     * 卡头ID
     */
    private Long cardBinId;

    /**
     * 卡BIN
     */
    private String cardBin;

    /**
     * 卡片类型
     */
    private String type;

    /**
     * 卡头名称
     */
    private String name;

    /**
     * 发行组织
     */
    private String network;

    /**
     * 发卡地区
     */
    private String country;

    /**
     * 支持 AVS 校验
     */
    private Boolean avs;

    /**
     * 支持 3DS 校验
     */
    private Boolean _3ds;

    /**
     * 单日消费限额
     */
    private BigDecimal dayPurchaseLimit;

    /**
     * 单笔消费限额
     */
    private BigDecimal singlePurchaseLimit;

    /**
     * 累计消费限额
     */
    private BigDecimal lifetimePurchaseLimit;

    /**
     * 是否维护
     */
    private Boolean maintain;

    /**
     * 权重
     */
    private Integer weigh;

    /**
     * 是否可用
     */
    private Boolean status;

    /**
     * 开卡价格
     */
    private BigDecimal createAmount;

    /**
     * 充值费率
     */
    private BigDecimal cardDepositFee;

    /**
     * 允许开卡
     */
    private Boolean allowCreate;

    /**
     * 允许充值
     */
    private Boolean allowIn;

    /**
     * 允许提现
     */
    private Boolean allowOut;

    /**
     * 允许冻结卡片
     */
    private Boolean allowSuspend;

    /**
     * 允许解冻卡片
     */
    private Boolean allowEnable;

    /**
     * 允许冻结卡片余额
     */
    private Boolean allowFrozen;

    /**
     * 允许解冻卡片余额
     */
    private Boolean allowUnfrozen;

    /**
     * 允许注销
     */
    private Boolean allowDestroy;

    /**
     * 首充最低限额
     */
    private BigDecimal createMinAmount;

    /**
     * 首充最高限额
     */
    private BigDecimal createMaxAmount;

    /**
     * 充值最低限额
     */
    private BigDecimal rechargeMinAmount;

    /**
     * 充值最高限额
     */
    private BigDecimal rechargeMaxAmount;

    /**
     * 提现最低限额
     */
    private BigDecimal withdrawMinAmount;

    /**
     * 提现最高限额
     */
    private BigDecimal withdrawMaxAmount;

    /**
     * 是否实体卡
     */
    private Boolean physical;

    /**
     * 销卡拒付次数阈值
     */
    private Integer refuseTimes;

    /**
     * 销卡拒付率阈值
     */
    private BigDecimal refuseRate;

    /**
     * 币种
     */
    private String currency;

    /**
     * 适用平台
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> scenes;
}
