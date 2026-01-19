package com.shop_service.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商户充值记录
 *
 * @author 啊祖
 * @date 2026-01-14 14:55
 **/
@Data
// 若有字段需要json映射得开启autoResultMap = true, 且标明@TableField(typeHandler = JacksonTypeHandler.class)注解
@TableName(value = "shop_recharge_record", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ShopRechargeRecord extends BaseEntity {
    /**
     * 商户ID
     */
    private Long shopId;

    /**
     * 商户号
     */
    private String shopNo;

    /**
     * 充值金额
     */
    private BigDecimal amount;

    /**
     * 手续费
     */
    private BigDecimal feeAmount;

    /**
     * 实际入账
     */
    private BigDecimal depositAmount;

    /**
     * 充值方式
     */
    private Integer manner;

    /**
     * 充值凭据
     */
    private String credential;
}
