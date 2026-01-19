package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 冻卡回调数据
 * <p>
 * 卡片被冻结后回调
 * @author 啊祖
 * @date 2026-01-16 14:02
 **/
@Data
public class ShopFreezeCardHookData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;
}
