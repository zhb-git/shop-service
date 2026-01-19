package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 冻卡结果回调数据
 *
 * @author 啊祖
 * @date 2026-01-15 17:34
 **/
@Data
public class VsFreezeCardResultCallbackData {
    /**
     * 交易ID
     */
    private String transactionId;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 卡片ID
     */
    private String cardId;

    /**
     * 卡号
     */
    private String cardNo;
}
