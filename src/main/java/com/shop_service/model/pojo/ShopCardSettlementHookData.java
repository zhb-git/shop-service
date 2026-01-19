package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 卡片超支结算回调数据
 * <p>
 * 当前卡片使用超支后银行结算(罚款)后回调
 * @author 啊祖
 * @date 2026-01-16 14:15
 **/
@Data
public class ShopCardSettlementHookData {
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
