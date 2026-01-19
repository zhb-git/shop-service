package com.shop_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 更新商户参数
 *
 * @author 啊祖
 * @date 2026-01-14 17:02
 **/
@Data
public class AdminUpdateShopQuery {
    /**
     * 商户ID
     */
    @NotNull(message = "请填写商户ID")
    private Long shopId;

    /**
     * 商户余额
     */
    @NotNull(message = "请填写商户余额")
    private BigDecimal balance;

    /**
     * 请求IP白名单
     */
    @NotNull(message = "请填写请求IP白名单")
    private List<String> ipWhitelist;

    /**
     * 回调url
     */
    @NotBlank(message = "请填写回调url")
    @Pattern(regexp = "^(http|https)://.*$", message = "请填写正确的回调地址")
    private String webhookUrl;

    /**
     * 回调超时 (毫秒)
     */
    @NotNull(message = "请填写回调超时 (毫秒)")
    @Min(value = 5000, message = "回调超时不能小于5000毫秒")
    private Integer webhookTimeoutMs;

    /**
     * 是否启用回调
     */
    @NotNull(message = "请填写是否启用回调")
    private Boolean webhookEnabled;

    /**
     * 充值费率
     */
    @NotNull(message = "请填写充值费率")
    @Min(value = 0, message = "充值费率不能小于0")
    private BigDecimal rechargeFee;

    /**
     * 是否启用
     */
    @NotNull(message = "请填写是否启用")
    private Boolean enabled;
}
