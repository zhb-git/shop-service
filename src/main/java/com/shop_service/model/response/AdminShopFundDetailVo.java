package com.shop_service.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户资金明细
 *
 * @author 啊祖
 * @date 2026-02-02 14:48
 **/
@Data
public class AdminShopFundDetailVo {
    /**
     * 商户ID
     */
    private Long shopId;

    /**
     * 商户号
     */
    private String shopNo;

    /**
     * 商户名字
     */
    private String shopName;

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

    /**
     * 产生时间
     */
    private LocalDateTime createTime;
}
