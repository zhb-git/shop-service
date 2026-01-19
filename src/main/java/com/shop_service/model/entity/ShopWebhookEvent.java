package com.shop_service.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 商户回调事件
 *
 * @author 啊祖
 * @date 2026-01-14 15:02
 **/
@Data
// 若有字段需要json映射得开启autoResultMap = true, 且标明@TableField(typeHandler = JacksonTypeHandler.class)注解
@TableName(value = "shop_webhook_event", autoResultMap = true)
@EqualsAndHashCode(callSuper = true)
public class ShopWebhookEvent extends BaseEntity {
    /**
     * 商户ID
     */
    private Long shopId;

    /**
     * 商户号
     */
    private String shopNo;

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
     * 回调地址(事件生成时快照, 避免商户改地址影响历史事件)
     */
    private String webhookUrl;

    /**
     * 事件载荷(JSON)
     */
    private String payload;

    /**
     * 当前投递状态
     * 0-待发送 1-发送中 2-发送成功 3-发送失败(待重试) 4-终止/放弃
     */
    private Integer status;

    /**
     * 已重试次数
     */
    private Integer retryCount;

    /**
     * 下一次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 最后一次发送时间
     */
    private LocalDateTime lastSendTime;

    /**
     * 最后一次错误信息(简短)
     */
    private String lastError;
}
