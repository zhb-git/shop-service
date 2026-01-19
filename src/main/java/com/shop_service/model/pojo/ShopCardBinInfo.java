package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 商户卡头信息
 *
 * @author 啊祖
 * @date 2026-01-17 15:08
 **/
@Data
public class ShopCardBinInfo {
    /**
     * 系统商户卡头ID
     */
    private Long id;

    /**
     * 商户ID
     */
    private Long shopId;

    /**
     * 卡头ID
     */
    private Long cardBinId;
}
