package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 冻卡订单
 *
 * @author 啊祖
 * @date 2026-01-15 17:33
 **/
@Data
public class FreezeCardOrder {
    /**
     * 商户信息
     */
    private ShopInfo shopInfo;

    /**
     * 系统卡片ID
     */
    private Long id;
}
