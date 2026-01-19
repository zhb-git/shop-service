package com.shop_service.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 重置商户公钥参数
 *
 * @author 啊祖
 * @date 2026-01-14 17:23
 **/
@Data
public class AdminResetShopPublicKeyQuery {
    /**
     * 商户ID
     */
    @NotNull(message = "请填写商户ID")
    private Long shopId;
}
