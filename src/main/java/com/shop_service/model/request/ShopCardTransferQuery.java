package com.shop_service.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 转账参数
 *
 * @author 啊祖
 * @date 2026-01-15 18:16
 **/
@Data
public class ShopCardTransferQuery {
    /**
     * 卡片ID
     */
    @NotNull(message = "请填写卡片ID")
    private String cardId;

    /**
     * 转账类型
     */
    @NotNull(message = "请填写转账类型 (1: 转入, 2: 转出)")
    private Integer type;

    /**
     * 小数只能精确到两位
     */
    @NotNull(message = "请填写转账金额")
    @DecimalMin(value = "0", inclusive = false, message = "转账金额必须大于0")
    private BigDecimal amount;
}
