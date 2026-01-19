package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡片注销回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 13:53
 **/
@Data
public class VsCardDestroyCallbackData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;
}
