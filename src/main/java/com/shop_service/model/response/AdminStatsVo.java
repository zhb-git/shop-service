package com.shop_service.model.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 统计信息
 *
 * @author 啊祖
 * @date 2026-02-02 15:36
 **/
@Data
public class AdminStatsVo {
    /**
     * 商户数量
     */
    private Long shopSize;

    /**
     * 商户充值金额
     */
    private BigDecimal shopRechargeAmount;

    /**
     * 商户充值手续费
     */
    private BigDecimal shopRechargeFeeAmount;

    /**
     * 卡片数量
     */
    private Long cardSize;

    /**
     * 正常卡片数量
     */
    private Long normalCardSize;

    /**
     * 冻结卡片数量
     */
    private Long frozenCardSize;

    /**
     * 注销卡片数量
     */
    private Long destroyCardSize;

    /**
     * 卡片消费金额
     */
    private BigDecimal cardConsumptionAmount;

    /**
     * 卡片转入金额
     */
    private BigDecimal cardTransferInAmount;

    /**
     * 卡片转出金额
     */
    private BigDecimal cardTransferOutAmount;

    /**
     * 卡片冻结金额
     */
    private BigDecimal cardFrozenAmount;

    /**
     * 卡片解冻金额
     */
    private BigDecimal cardUnfrozenAmount;

    /**
     * 卡片撤销金额
     */
    private BigDecimal cardReversalAmount;

    /**
     * 卡片退款金额
     */
    private BigDecimal cardCreditAmount;

    /**
     * 卡片授权金额
     */
    private BigDecimal cardFeeConsumptionAmount;
}
