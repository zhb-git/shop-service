package com.shop_service.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户操作记录
 *
 * @author 啊祖
 * @date 2026-02-02 14:16
 **/
@Data
public class AdminShopRechargeRecordVo {
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

    /**
     * 充值时间
     */
    private LocalDateTime createTime;
}
