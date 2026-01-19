package com.shop_service.model.pojo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 卡片3DS回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 14:25
 **/
@Data
public class VsCard3DSCallbackData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 完整卡号
     */
    private String cardNo;

    /**
     * 内部订单ID
     */
    private String orderId;

    /**
     * 行为 ID
     * 跳转阶段存在
     */
    private String actionId;

    /**
     * 交易场景或商户名称
     */
    private String detail;

    /**
     * 交易金额
     */
    private BigDecimal amount;

    /**
     * 交易币种
     */
    private String currency;

    /**
     * 3DS 验证阶段
     * OTP:        验证码验证
     * FORWARDING: 跳转验证
     */
    private String stage;

    /**
     * 3DS 跳转 URL
     * 跳转阶段存在
     */
    private String forwardingUrl;

    /**
     * 3DS 验证码
     * OTP阶段存在
     */
    private String otp;

    /**
     * 跳转链接生效时间
     * 跳转阶段存在
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime callbackTime;

    /**
     * 跳转链接过期时间
     * 跳转阶段存在
     */
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expirationTime;
}
