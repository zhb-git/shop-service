package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.model.entity.ShopFundDetail;

/**
 * 商户资金明细服务层
 *
 * @author 啊祖
 * @date 2026-01-14 16:34
 **/
public interface IShopFundDetailService extends IService<ShopFundDetail> {
    /**
     * 新增资金明细
     * @param detail 明细
     */
    void create(ShopFundDetail detail);

    /**
     * 更新bizNo
     * @param id    ID
     * @param bizNo 业务单号
     */
    void updateBizNo(Long id, String bizNo);
}
