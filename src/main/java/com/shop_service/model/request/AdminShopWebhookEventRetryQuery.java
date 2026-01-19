package com.shop_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 商户回调事件重试参数
 *
 * @author 啊祖
 * @date 2026-01-16 20:16
 **/
@Data
public class AdminShopWebhookEventRetryQuery {
    /**
     * 事件系统ID
     */
    @NotNull(message = "事件系统ID")
    @Min(value = 1, message = "最少重试一个ID")
    private List<Long> idList;
}
