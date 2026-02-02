package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.model.entity.ShopRechargeRecord;
import com.shop_service.model.request.AdminShopRechargeRecordPageQuery;
import com.shop_service.model.response.AdminShopRechargeRecordVo;
import com.shop_service.model.response.RespPage;

/**
 * 商户充值记录服务层
 *
 * @author 啊祖
 * @date 2026-01-14 16:35
 **/
public interface IShopRechargeRecordService extends IService<ShopRechargeRecord> {
    /**
     * 新增充值记录
     * @param record 记录
     */
    void create(ShopRechargeRecord record);

    /**
     * 分页查询
     * @param query 参数
     * @return 结果
     */
    RespPage<AdminShopRechargeRecordVo> getAdminShopRechargeRecordVoPage(AdminShopRechargeRecordPageQuery query);
}
