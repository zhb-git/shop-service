package com.shop_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 解冻卡片参数
 *
 * @author 啊祖
 * @date 2026-01-15 17:46
 **/
@Data
public class ShopUnfreezeCardQuery {
    /**
     * 卡片ID
     */
    @NotBlank(message = "请填写卡片ID")
    private String cardId;
}
