package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.model.entity.ShopCardFundDetail;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.pojo.VsCardFundDetailCallbackData;
import com.shop_service.model.request.ShopCardFundDetailPageQuery;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopCardFundDetailVo;

/**
 * 商户卡片资金明细服务层
 *
 * @author 啊祖
 * @date 2026-01-14 16:32
 **/
public interface IShopCardFundDetailService extends IService<ShopCardFundDetail> {
    /**
     * 卡片资金明细回调
     * @param data 数据
     */
    void cardFundDetailCallback(VsCardFundDetailCallbackData data);

    /**
     * 分页查询
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 结果
     */
    RespPage<ShopCardFundDetailVo> getShopCardFundDetailVoPage(ShopInfo shopInfo, ShopCardFundDetailPageQuery query);
}
