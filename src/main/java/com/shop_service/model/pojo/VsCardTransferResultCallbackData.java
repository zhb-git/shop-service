package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 卡片转账结果回调数据
 *
 * @author 啊祖
 * @date 2026-01-15 18:51
 **/
@Data
public class VsCardTransferResultCallbackData {
    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 转账类型 (1: 转入, 2: 转出)
     */
    private Integer transferType;

    /**
     * 转账金额
     */
    private BigDecimal amount;
}
