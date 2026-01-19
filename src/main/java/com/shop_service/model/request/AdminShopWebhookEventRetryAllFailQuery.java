package com.shop_service.model.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 重试商户所有失败回调事件参数
 *
 * @author 啊祖
 * @date 2026-01-17 13:41
 **/
@Data
public class AdminShopWebhookEventRetryAllFailQuery {
    /**
     * 商户ID
     */
    @NotNull(message = "请填写商户ID")
    private Long shopId;
}
