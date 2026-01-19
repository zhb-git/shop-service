package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 解冻卡片结果回调数据
 * <p>
 * 调用解冻卡片接口, 银行出结果后回调
 * @author 啊祖
 * @date 2026-01-15 17:57
 **/
@Data
public class ShopUnfreezeCardResultHookData {
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
