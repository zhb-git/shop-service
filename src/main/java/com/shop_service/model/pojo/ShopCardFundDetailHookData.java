package com.shop_service.model.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卡片资金明细回调
 * <p>
 * 当卡片产生新的交易后回调
 * @author 啊祖
 * @date 2026-01-16 14:58
 **/
@Data
public class ShopCardFundDetailHookData {
    /**
     * 账单类型
     * Consumption 消费
     * TransferIn 转入
     * TransferOut 转出
     * Frozen 冻结卡片金额
     * UnFrozen 解冻卡片金额
     * Reversal 撤销
     * Credit 退款
     * Fee_Consumption 授权
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
     * Closed 已完成
     * Pending 处理中
     * Fail 失败
     */
    private String status;

    /**
     * 交易发生的时间
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;
}
