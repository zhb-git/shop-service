package com.shop_service.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卡片资金明细
 *
 * @author 啊祖
 * @date 2026-01-16 16:04
 **/
@Data
public class ShopCardFundDetailVo {
    /**
     * 账单类型
     */
    private String type;

    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 实际从卡内扣除/结算的金额
     */
    private BigDecimal amount;

    /**
     * 该笔交易所产生的手续费
     */
    private BigDecimal fee;

    /**
     * 结算币种
     */
    private String currency;

    /**
     * 原始订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 原始订单币种
     */
    private String orderCurrency;

    /**
     * 交易详情描述
     */
    private String detail;

    /**
     * 商户行业代码
     */
    private String mcc;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 商户所属国家代码
     */
    private String merchantCountry;

    /**
     * 关联的原始交易流水ID
     */
    private String relatedCardTransactionId;

    /**
     * 账单状态
     */
    private String status;

    /**
     * 交易发生的时间
     */
    private LocalDateTime transactionTime;
}
