package com.shop_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopFundDetailMapper;
import com.shop_service.model.entity.ShopFundDetail;
import com.shop_service.service.IShopFundDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 商户资金明细服务实现类
 *
 * @author 啊祖
 * @date 2026-01-14 16:34
 **/
@Slf4j
@Service
public class ShopFundDetailServiceImpl extends ServiceImpl<ShopFundDetailMapper, ShopFundDetail> implements IShopFundDetailService {
    @Override
    public void create(ShopFundDetail detail) {
        if (baseMapper.insert(detail) != 1) {
            throw new BizException("资金明细新增失败");
        }
    }

    @Override
    public void updateBizNo(Long id, String bizNo) {
        LambdaUpdateWrapper<ShopFundDetail> uw = new LambdaUpdateWrapper<>();
        uw.set(ShopFundDetail::getBizNo, bizNo)
                .set(ShopFundDetail::getUpdateTime, LocalDateTime.now())
                .eq(ShopFundDetail::getId, id);
        if (baseMapper.update(uw) != 1) {
            throw new BizException("资金明细业务单号更新失败");
        }
    }
}
