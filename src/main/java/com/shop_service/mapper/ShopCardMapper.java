package com.shop_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop_service.model.entity.ShopCard;
import com.shop_service.model.pojo.CardInfo;
import com.shop_service.model.request.AdminShopCardPageQuery;
import com.shop_service.model.response.AdminShopCardVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户卡片Mapper
 *
 * @author 啊祖
 * @date 2026-01-14 16:29
 **/
public interface ShopCardMapper extends BaseMapper<ShopCard> {
    /**
     * 查询所有未注销卡片信息列表
     *
     * @return 卡片信息列表
     */
    List<CardInfo> selectNotDestroyCardInfoList();

    /**
     * 分页查询
     * @param page  分页
     * @param query 参数
     * @return 结果
     */
    IPage<AdminShopCardVo> selectAdminShopCardVoPage(Page<?> page, @Param("query") AdminShopCardPageQuery query);
}
