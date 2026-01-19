package com.shop_service.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建商户参数
 *
 * @author 啊祖
 * @date 2026-01-14 16:50
 **/
@Data
public class AdminCreateShopQuery {
    /**
     * 商户名字
     */
    @NotBlank(message = "请填写商户名字")
    private String name;
}
