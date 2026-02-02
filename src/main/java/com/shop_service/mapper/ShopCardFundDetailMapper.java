package com.shop_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop_service.model.entity.ShopCardFundDetail;
import com.shop_service.model.request.AdminShopCardFundDetailPageQuery;
import com.shop_service.model.response.AdminShopCardFundDetailVo;
import org.apache.ibatis.annotations.Param;

/**
 * 商户卡片资金明细
 *
 * @author 啊祖
 * @date 2026-01-14 16:31
 **/
public interface ShopCardFundDetailMapper extends BaseMapper<ShopCardFundDetail> {
    /**
     * 分页查询
     * @param page  分页
     * @param query 参数
     * @return 结果
     */
    IPage<AdminShopCardFundDetailVo> selectAdminShopCardFundDetailVoPage(Page<?> page, @Param("query") AdminShopCardFundDetailPageQuery query);
}
