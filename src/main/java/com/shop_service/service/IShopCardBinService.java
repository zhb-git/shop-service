package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.model.entity.ShopCardBin;
import com.shop_service.model.pojo.VsCardBinStatusCallbackData;
import com.shop_service.model.request.AdminDeleteShopCardBinQuery;
import com.shop_service.model.request.AdminSetShopCardBinQuery;
import com.shop_service.model.request.AdminShopCardBinPageQuery;
import com.shop_service.model.request.AdminUpdateShopCardBinOpenAmountQuery;
import com.shop_service.model.response.AdminShopCardBinVo;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopCardBinVo;

import java.util.List;

/**
 * 商户卡头服务层
 *
 * @author 啊祖
 * @date 2026-01-14 16:31
 **/
public interface IShopCardBinService extends IService<ShopCardBin> {
    /**
     * 设置商户卡头
     * @param query 参数
     */
    void setShopCardBin(AdminSetShopCardBinQuery query);

    /**
     * 获取商户卡头列表
     * @param query 参数
     * @return 结果
     */
    RespPage<AdminShopCardBinVo> getAdminShopCardBinVoList(AdminShopCardBinPageQuery query);

    /**
     * 更新商户卡头开卡价格
     * @param query 参数
     */
    void updateShopCardBinOpenAmount(AdminUpdateShopCardBinOpenAmountQuery query);

    /**
     * 删除商户卡头
     * @param query 参数
     */
    void deleteShopCardBin(AdminDeleteShopCardBinQuery query);

    /**
     * 获取商户卡头列表
     * @param shopId 商户ID
     * @return 结果
     */
    List<ShopCardBinVo> getShopCardBinVoList(Long shopId);

    /**
     * 获取卡头
     * @param shopId    商户ID
     * @param cardBinId 卡头ID
     * @return 卡头
     */
    ShopCardBin getByShopIdAndCardBinId(Long shopId, Long cardBinId);

    /**
     * 卡头状态变更回调
     * @param data 数据
     */
    void cardBinStatusCallback(VsCardBinStatusCallbackData data);
}
