package com.shop_service.model.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卡片资金明细回调
 *
 * @author 啊祖
 * @date 2026-01-16 14:57
 **/
@Data
public class VsCardFundDetailCallbackData {
    /**
     * 账单类型
     */
    private String type;

    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 完整卡号
     */
    private String cardNo;

    /**
     * 实际卡内扣款/入账
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
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;
}
