package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡片帮定平台回调数据
 * <p>
 * 当卡片被绑定到某个平台后回调
 * @author 啊祖
 * @date 2026-01-16 14:21
 **/
@Data
public class ShopCardBindPlatformHookData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;

    /**
     * 平台行业代码 (Merchant Category Code)
     */
    private String mcc;

    /**
     * 平台详情
     */
    private String detail;
}
