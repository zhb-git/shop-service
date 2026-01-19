package com.shop_service.model.pojo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 波场地址监控转账
 *
 * @author 啊祖
 * @date 2026-01-14 19:46
 **/
@Data
public class TronAddressControlTransfer {
    /**
     * 监控地址
     */
    @NotBlank(message = "请填写监控地址")
    private String controlAddress;

    /**
     * 对方地址
     */
    @NotBlank(message = "请填写对方地址")
    private String opposingAddress;

    /**
     * 转账类型
     * 1: 转入 2: 转出
     */
    @NotNull(message = "请填写转账类型")
    private Integer type;

    /**
     * 金额
     */
    @NotNull(message = "请填写金额")
    private BigDecimal amount;

    /**
     * 币种
     * TRX USDT
     */
    @NotBlank(message = "请填写币种")
    private String coin;

    /**
     * 回调数据
     */
    @NotBlank(message = "请填写回调数据")
    private String callbackData;

    /**
     * 转账哈希
     */
    @NotBlank(message = "请填写转账哈希")
    private String transferHash;
}
