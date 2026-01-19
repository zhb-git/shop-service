package com.shop_service.model.pojo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 卡片超支回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 14:10
 **/
@Data
public class VsCardOverspendCallbackData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 超支的金额
     */
    private BigDecimal amount;
}
