package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡片注销回调数据
 * <p>
 * 卡片被注销后回调
 * @author 啊祖
 * @date 2026-01-16 13:52
 **/
@Data
public class ShopCardDestroyHookData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;
}
