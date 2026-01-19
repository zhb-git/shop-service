package com.shop_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 商户注销卡片参数
 *
 * @author 啊祖
 * @date 2026-01-15 16:55
 **/
@Data
public class ShopDestroyCardQuery {
    /**
     * 卡片ID
     */
    @NotBlank(message = "请填写卡片ID")
    private String cardId;
}
