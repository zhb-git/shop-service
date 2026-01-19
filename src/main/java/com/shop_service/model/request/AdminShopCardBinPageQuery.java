package com.shop_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商户卡头分页查询参数
 *
 * @author 啊祖
 * @date 2026-01-14 23:28
 **/
@Data
public class AdminShopCardBinPageQuery {
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
}
