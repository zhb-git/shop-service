package com.shop_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop_service.model.entity.ShopFundDetail;
import com.shop_service.model.request.AdminShopFundDetailPageQuery;
import com.shop_service.model.response.AdminShopFundDetailVo;
import org.apache.ibatis.annotations.Param;

/**
 * 商户资金明细Mapper
 *
 * @author 啊祖
 * @date 2026-01-14 16:33
 **/
public interface ShopFundDetailMapper extends BaseMapper<ShopFundDetail> {
    /**
     * 分页查询
     * @param page  分页
     * @param query 参数
     * @return 结果
     */
    IPage<AdminShopFundDetailVo> selectAdminShopFundDetailVoPage(Page<?> page, @Param("query") AdminShopFundDetailPageQuery query);
}
