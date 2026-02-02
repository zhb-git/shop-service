package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.model.entity.ShopRechargeRecord;
import com.shop_service.model.request.AdminShopRechargeRecordPageQuery;
import com.shop_service.model.response.AdminShopRechargeRecordVo;
import com.shop_service.model.response.RespPage;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    /**
     * 查询充值金额
     * @param date 时间
     * @return 总金额
     */
    BigDecimal getTotalAmount(LocalDate date);

    /**
     * 查询充值手续费
     * @param date 时间
     * @return 总手续费
     */
    BigDecimal getTotalFeeAmount(LocalDate date);
}
