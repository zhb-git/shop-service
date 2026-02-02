package com.shop_service.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop_service.common.core.RespPageConvert;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopRechargeRecordMapper;
import com.shop_service.model.entity.ShopRechargeRecord;
import com.shop_service.model.request.AdminShopRechargeRecordPageQuery;
import com.shop_service.model.response.AdminShopRechargeRecordVo;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopRechargeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商户充值记录服务实现类
 *
 * @author 啊祖
 * @date 2026-01-14 16:36
 **/
@Slf4j
@Service
public class ShopRechargeRecordServiceImpl extends ServiceImpl<ShopRechargeRecordMapper, ShopRechargeRecord> implements IShopRechargeRecordService {
    @Override
    public void create(ShopRechargeRecord record) {
        if (baseMapper.insert(record) != 1) {
            throw new BizException("商户充值记录新增失败");
        }
    }

    @Override
    public RespPage<AdminShopRechargeRecordVo> getAdminShopRechargeRecordVoPage(AdminShopRechargeRecordPageQuery query) {
        IPage<AdminShopRechargeRecordVo> page = baseMapper.selectAdminShopRechargeRecordVoPage(new Page<>(query.getPageNum(), query.getPageSize()), query);
        return RespPageConvert.convert(page, AdminShopRechargeRecordVo.class);
    }

    @Override
    public BigDecimal getTotalAmount(LocalDate date) {
        return baseMapper.selectTotalAmount(date);
    }

    @Override
    public BigDecimal getTotalFeeAmount(LocalDate date) {
        return baseMapper.selectTotalFeeAmount(date);
    }
}
