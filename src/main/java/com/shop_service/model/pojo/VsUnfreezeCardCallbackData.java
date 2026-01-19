package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 解冻卡片回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 14:05
 **/
@Data
public class VsUnfreezeCardCallbackData {
    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;
}
