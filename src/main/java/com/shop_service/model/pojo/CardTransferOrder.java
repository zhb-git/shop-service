package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡片转账订单
 *
 * @author 啊祖
 * @date 2026-01-15 18:26
 **/
@Data
public class CardTransferOrder {
    /**
     * 商户信息
     */
    private ShopInfo shopInfo;
}
