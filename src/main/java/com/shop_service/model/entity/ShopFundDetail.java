package com.shop_service.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商户资金明细
 *
 * @author 啊祖
 * @date 2026-01-14 14:57
 **/
@Data
// 若有字段需要json映射得开启autoResultMap = true, 且标明@TableField(typeHandler = JacksonTypeHandler.class)注解
@TableName(value = "shop_fund_detail", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ShopFundDetail extends BaseEntity {
    /**
     * 商户ID
     */
    private Long shopId;

    /**
     * 商户号
     */
    private String shopNo;

    /**
     * 操作类型
     */
    private Integer type;

    /**
     * 变动金额, 正数表示增加, 负数表示减少
     */
    private BigDecimal amount;

    /**
     * 操作前余额
     */
    private BigDecimal balanceBefore;

    /**
     * 操作后余额
     */
    private BigDecimal balanceAfter;

    /**
     * 关联业务单号, 来源单号
     */
    private String bizNo;

    /**
     * 备注
     */
    private String remark;
}
