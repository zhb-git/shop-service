package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 解冻卡片结果回调数据
 *
 * @author 啊祖
 * @date 2026-01-15 17:52
 **/
@Data
public class VsUnfreezeCardResultCallbackData {
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
