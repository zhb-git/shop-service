package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 销卡订单
 *
 * @author 啊祖
 * @date 2026-01-15 17:10
 **/
@Data
public class DestroyCardOrder {
    /**
     * 商户信息
     */
    private ShopInfo shopInfo;

    /**
     * 系统卡片ID
     */
    private Long id;
}
