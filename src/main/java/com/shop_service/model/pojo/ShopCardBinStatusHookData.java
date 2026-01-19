package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡头状态变更回调数据
 * <p>
 * 当卡头状态发生变化后回调
 * @author 啊祖
 * @date 2026-01-16 14:31
 **/
@Data
public class ShopCardBinStatusHookData {
    /**
     * 卡头ID
     */
    private Long id;

    /**
     * 卡Bin
     */
    private String bin;

    /**
     * 是否启用
     */
    private Boolean status;

    /**
     * 是否维护中
     */
    private Boolean maintain;
}
