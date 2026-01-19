package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 冻卡回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 14:01
 **/
@Data
public class VsFreezeCardCallbackData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;
}
