package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡片信息
 *
 * @author 啊祖
 * @date 2026-01-17 17:49
 **/
@Data
public class CardInfo {
    /**
     * 卡片系统ID
     */
    private Long id;

    /**
     * 卡片ID
     */
    private String cardId;
}
