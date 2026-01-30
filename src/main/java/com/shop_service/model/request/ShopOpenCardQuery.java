package com.shop_service.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户开卡参数
 *
 * @author 啊祖
 * @date 2026-01-15 13:46
 **/
@Data
public class ShopOpenCardQuery {
    /**
     * 卡头ID
     */
    @NotNull(message = "请填写卡头ID")
    private Long binId;

    /**
     * 预留金额
     */
    @NotNull(message = "请填写预留金额")
    @DecimalMin(value = "0", inclusive = false, message = "预留金额必须大于0")
    private BigDecimal reserveAmount;
}
