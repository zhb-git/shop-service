package com.shop_service.model.request;

import com.alibaba.fastjson2.JSONObject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * vs回调参数
 *
 * @author 啊祖
 * @date 2026-01-15 14:43
 **/
@Data
public class VsCallbackQuery {
    /**
     * 回调ID
     */
    @NotNull(message = "请填写回调ID")
    private String id;

    /**
     * 回调类型
     */
    @NotBlank(message = "请填写回调类型")
    private String type;

    /**
     * 回调数据
     */
    @NotNull(message = "请填写回调数据")
    private JSONObject data;
}
