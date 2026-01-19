package com.shop_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop_service.model.entity.ShopCardBin;
import com.shop_service.model.pojo.ShopCardBinInfo;
import com.shop_service.model.request.AdminShopCardBinPageQuery;
import com.shop_service.model.response.AdminShopCardBinVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商户卡头Mapper
 *
 * @author 啊祖
 * @date 2026-01-14 16:30
 **/
public interface ShopCardBinMapper extends BaseMapper<ShopCardBin> {
    /**
     * 分页查询商户卡头
     *
     * @param query 参数
     * @return 结果
     */
    IPage<AdminShopCardBinVo> selectAdminShopCardBinVoPage(Page<?> page, @Param("query") AdminShopCardBinPageQuery query);

    /**
     * 查询此卡段所有的商户ID
     * @param cardBinId 卡头ID
     * @return 商户ID列表
     */
    List<Long> selectShopIdByCardBinId(@Param("cardBinId") Long cardBinId);

    /**
     * 查询此卡头ID的所有商户卡头信息
     * @param cardBinId 卡头ID
     * @return 商户卡头信息列表
     */
    List<ShopCardBinInfo> selectShopCardBinInfoByCardBinId(@Param("cardBinId") Long cardBinId);
}
