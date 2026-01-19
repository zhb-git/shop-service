package com.shop_service.model.request;

import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 波场地址监控回调参数
 *
 * @author 啊祖
 * @date 2026-01-14 19:45
 **/
@Data
public class TronAddressControlCallbackQuery {
    /**
     * 类型
     * 1: 转账
     */
    @NotNull(message = "请填写类型")
    private Integer type;

    /**
     * 数据
     */
    @NotNull(message = "请填写数据")
    private JSONObject data;
}
