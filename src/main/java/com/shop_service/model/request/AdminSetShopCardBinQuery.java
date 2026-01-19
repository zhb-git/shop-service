package com.shop_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 设置商户卡头参数
 *
 * @author 啊祖
 * @date 2026-01-14 22:44
 **/
@Data
public class AdminSetShopCardBinQuery {
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
    @Min(value = 0, message = "开卡价格不能小于0")
    private BigDecimal createAmount;
}
