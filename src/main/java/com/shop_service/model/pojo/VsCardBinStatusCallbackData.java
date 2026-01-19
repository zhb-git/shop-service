package com.shop_service.model.pojo;

import lombok.Data;

/**
 * 卡头状态变更回调数据
 *
 * @author 啊祖
 * @date 2026-01-16 14:30
 **/
@Data
public class VsCardBinStatusCallbackData {
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
