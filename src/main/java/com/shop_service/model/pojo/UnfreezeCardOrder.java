package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 解冻卡片订单
 *
 * @author 啊祖
 * @date 2026-01-15 17:49
 **/
@Data
public class UnfreezeCardOrder {
    /**
     * 商户信息
     */
    private ShopInfo shopInfo;

    /**
     * 系统卡片ID
     */
    private Long id;
}
