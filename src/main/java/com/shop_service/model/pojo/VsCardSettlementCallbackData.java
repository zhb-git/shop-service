package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 卡片超支结算回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 14:14
 **/
@Data
public class VsCardSettlementCallbackData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 卡片被扣除的金额
     */
    private BigDecimal amount;
}
