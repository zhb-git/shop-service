package com.shop_service.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 删除商户卡头参数
 *
 * @author 啊祖
 * @date 2026-01-14 23:07
 **/
@Data
public class AdminDeleteShopCardBinQuery {
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
}
