package com.shop_service.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新商户卡头开卡价格参数
 *
 * @author 啊祖
 * @date 2026-01-14 23:34
 **/
@Data
public class AdminUpdateShopCardBinOpenAmountQuery {
    /**
     * 商户ID
     */
    @NotNull(message = "请填写商户ID")
    private Long shopId;

    /**
     * 卡头ID
     */
    @NotNull(message = "请填写卡头ID")
    private Long cardBinId;

    /**
     * 开卡价格
     */
    @NotNull(message = "请填写开卡价格")
    @DecimalMin(value = "0", inclusive = false, message = "开卡价格必须大于0")
    private BigDecimal createAmount;
}
