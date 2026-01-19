package com.shop_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商户卡头分页查询参数
 *
 * @author 啊祖
 * @date 2026-01-16 19:57
 **/
@Data
public class AdminShopWebhookEventPageQuery {
    /**
     * 分页页码
     */
    @NotNull(message = "页码不能为空")
    @Min(value = 1, message = "页码必须>=1")
    private Integer pageNum;

    /**
     * 每页数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须>=1")
    private Integer pageSize;

    /**
     * 商户ID
     */
    private Long shopId;

    /**
     * 商户号 (模糊查询)
     */
    private String shopNo;

    /**
     * 商户名字 (模糊查询)
     */
    private String shopName;

    /**
     * 事件ID(对外幂等标识), 建议全局唯一
     * 商户可用它做幂等处理, 我方也可用于定位/排查
     */
    private String eventId;

    /**
     * 事件类型
     * 例如: RECHARGE_SUCCESS, CARD_CREATE, TRANSACTION_POSTED 等
     */
    private String eventType;

    /**
     * 当前投递状态
     * 0-待发送 1-发送中 2-发送成功 3-发送失败(待重试) 4-终止/放弃
     */
    private Integer status;
}
