package com.shop_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop_service.model.entity.ShopRechargeRecord;
import com.shop_service.model.request.AdminShopRechargeRecordPageQuery;
import com.shop_service.model.response.AdminShopRechargeRecordVo;
import org.apache.ibatis.annotations.Param;

/**
 * 商户充值记录Mapper
 *
 * @author 啊祖
 * @date 2026-01-14 16:35
 **/
public interface ShopRechargeRecordMapper extends BaseMapper<ShopRechargeRecord> {
    /**
     * 分页查询
     * @param page  分页
     * @param query 参数
     * @return 结果
     */
    IPage<AdminShopRechargeRecordVo> selectAdminShopRechargeRecordVoPage(Page<?> page, @Param("query") AdminShopRechargeRecordPageQuery query);
}
