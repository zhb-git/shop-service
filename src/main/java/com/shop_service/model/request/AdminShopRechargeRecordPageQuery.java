package com.shop_service.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 商户充值记录分页查询参数
 *
 * @author 啊祖
 * @date 2026-02-02 14:18
 **/
@Data
public class AdminShopRechargeRecordPageQuery {
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
     * 充值凭据 (模糊查询)
     */
    private String credential;
}
