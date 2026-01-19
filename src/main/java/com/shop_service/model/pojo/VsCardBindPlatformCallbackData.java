package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡邦平台回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 14:21
 **/
@Data
public class VsCardBindPlatformCallbackData {
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
