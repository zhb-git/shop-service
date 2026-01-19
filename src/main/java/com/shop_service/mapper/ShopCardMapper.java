package com.shop_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shop_service.model.entity.ShopCard;
import com.shop_service.model.pojo.CardInfo;

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
     * @return 卡片信息列表
     */
    List<CardInfo> selectNotDestroyCardInfoList();
}
