package com.shop_service.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopRechargeRecordMapper;
import com.shop_service.model.entity.ShopRechargeRecord;
import com.shop_service.service.IShopRechargeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
}
