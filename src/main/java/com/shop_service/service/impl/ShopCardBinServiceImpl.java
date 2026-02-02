package com.shop_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop_service.common.constant.LockServiceType;
import com.shop_service.common.constant.ShopWebhookEventType;
import com.shop_service.common.core.LockKeyProduce;
import com.shop_service.common.core.RedissonLockExecutor;
import com.shop_service.common.core.RespPageConvert;
import com.shop_service.common.core.VsApi;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopCardBinMapper;
import com.shop_service.model.entity.ShopCardBin;
import com.shop_service.model.pojo.*;
import com.shop_service.model.request.AdminDeleteShopCardBinQuery;
import com.shop_service.model.request.AdminSetShopCardBinQuery;
import com.shop_service.model.request.AdminShopCardBinPageQuery;
import com.shop_service.model.request.AdminUpdateShopCardBinOpenAmountQuery;
import com.shop_service.model.response.AdminShopCardBinVo;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopCardBinVo;
import com.shop_service.service.IShopCardBinService;
import com.shop_service.service.IShopService;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 商户卡头服务实现类
 *
 * @author 啊祖
 * @date 2026-01-14 16:31
 **/
@Slf4j
@Service
public class ShopCardBinServiceImpl extends ServiceImpl<ShopCardBinMapper, ShopCardBin> implements IShopCardBinService {
    @Resource
    private VsApi vsApi;

    @Resource
    private IShopService shopService;

    @Resource
    private IShopWebhookEventService shopWebhookEventService;

    @Resource
    private RedissonLockExecutor redissonLockExecutor;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // 初始化同步所有卡头
        initSyncBin();
        log.info("所有商户卡头初始化同步完成");
    }

    /**
     * 初始化同步所有商户卡头 (硬性同步, 不能异步)
     */
    private void initSyncBin() {
        // 获取vs所有卡头
        for (VsCardBin bin : vsApi.getCardBinList()) {
            // 同步卡头
            for (ShopCardBinInfo binInfo : baseMapper.selectShopCardBinInfoByCardBinId(bin.getId())) {
                // 加锁执行
                String lock = LockKeyProduce.produce(LockServiceType.SHOP_CARD_BIN, binInfo.getShopId(), binInfo.getCardBinId());
                redissonLockExecutor.execute(lock, () -> {
                    ShopCardBin shopCardBin = baseMapper.selectById(binInfo.getId());
                    // 同步属性 (卡头ID 卡头BIN 开卡价格除外)
                    BeanUtils.copyProperties(bin, shopCardBin, "id", "bin", "createAmount");
                    if (baseMapper.updateById(shopCardBin) != 1) {
                        throw new BizException("商户卡头更新失败");
                    }
                });
            }
        }
    }

    @Override
    public void setShopCardBin(AdminSetShopCardBinQuery query) {
        // 校验商户是否存在
        ShopInfo shopInfo = checkShop(query.getShopId());
        // 查询所有卡头
        List<VsCardBin> binList = vsApi.getCardBinList();
        // 校验卡头是否存在
        VsCardBin cardBin = null;
        for (VsCardBin bin : binList) {
            if (bin.getId().equals(query.getCardBinId())) {
                cardBin = bin;
                break;
            }
        }
        if (cardBin == null) {
            throw new BizException("卡头不存在");
        }
        final VsCardBin bin = cardBin;
        // 加锁执行
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_CARD_BIN, shopInfo.getId(), query.getCardBinId());
        redissonLockExecutor.execute(lock, () -> {
            // 校验商户是否已拥有此卡头
            LambdaQueryWrapper<ShopCardBin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ShopCardBin::getShopId, query.getShopId())
                    .eq(ShopCardBin::getCardBinId, query.getCardBinId());
            if (baseMapper.exists(wrapper)) {
                throw new BizException("商户已拥有此卡头");
            }
            // 设置卡头
            ShopCardBin shopCardBin = new ShopCardBin();
            BeanUtils.copyProperties(bin, shopCardBin, "id", "bin", "createAmount");
            shopCardBin.setShopId(shopInfo.getId());
            shopCardBin.setShopNo(shopInfo.getNo());
            shopCardBin.setCardBinId(bin.getId());
            shopCardBin.setCardBin(bin.getBin());
            shopCardBin.setCreateAmount(query.getCreateAmount());
            if (baseMapper.insert(shopCardBin) != 1) {
                throw new BizException("商户卡头设置失败");
            }
            log.info("商户成功设置卡头 - 商户ID={}, 卡头ID={}", shopInfo.getId(), bin.getId());
        });
    }

    @Override
    public RespPage<AdminShopCardBinVo> getAdminShopCardBinVoList(AdminShopCardBinPageQuery query) {
        IPage<AdminShopCardBinVo> page = baseMapper.selectAdminShopCardBinVoPage(new Page<>(query.getPageNum(), query.getPageSize()), query);
        return RespPageConvert.convert(page, AdminShopCardBinVo.class);
    }

    @Override
    public void updateShopCardBinOpenAmount(AdminUpdateShopCardBinOpenAmountQuery query) {
        // 校验商户是否存在
        ShopInfo shopInfo = checkShop(query.getShopId());
        // 加锁执行
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_CARD_BIN, shopInfo.getId(), query.getCardBinId());
        redissonLockExecutor.execute(lock, () -> {
            // 校验卡头是否存在
            ShopCardBin shopCardBin = checkCardBin(shopInfo.getId(), query.getCardBinId());
            shopCardBin.setCreateAmount(query.getCreateAmount());
            if (baseMapper.updateById(shopCardBin) != 1) {
                throw new BizException("商户卡头更新失败");
            }
        });
    }

    @Override
    public void deleteShopCardBin(AdminDeleteShopCardBinQuery query) {
        // 校验商户是否存在
        ShopInfo shopInfo = checkShop(query.getShopId());
        // 加锁执行
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_CARD_BIN, shopInfo.getId(), query.getCardBinId());
        redissonLockExecutor.execute(lock, () -> {
            // 校验卡头是否存在
            ShopCardBin shopCardBin = checkCardBin(shopInfo.getId(), query.getCardBinId());
            // 删除卡头
            if (baseMapper.deleteById(shopCardBin.getId()) != 1) {
                throw new BizException("商户卡头删除失败");
            }
        });
    }

    @Override
    public List<ShopCardBinVo> getShopCardBinVoList(Long shopId) {
        LambdaQueryWrapper<ShopCardBin> qw = new LambdaQueryWrapper<>();
        qw.eq(ShopCardBin::getShopId, shopId);
        List<ShopCardBinVo> voList = new ArrayList<>();
        for (ShopCardBin bin : baseMapper.selectList(qw)) {
            ShopCardBinVo vo = new ShopCardBinVo();
            BeanUtils.copyProperties(bin, vo);
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public ShopCardBin getByShopIdAndCardBinId(Long shopId, Long cardBinId) {
        LambdaQueryWrapper<ShopCardBin> qw = new LambdaQueryWrapper<>();
        qw.eq(ShopCardBin::getShopId, shopId)
                .eq(ShopCardBin::getCardBinId, cardBinId);
        return baseMapper.selectOne(qw);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void cardBinStatusCallback(VsCardBinStatusCallbackData data) {
        log.info("vs卡头状态变更回调 - 卡头ID={}, 卡头状态={}, 是否可用={}", data.getId(), data.getStatus(), data.getMaintain());
        // 查询拥有此卡头的所有商户
        List<Long> shopIdList = baseMapper.selectShopIdByCardBinId(data.getId());
        ShopCardBinStatusHookData hookData = new ShopCardBinStatusHookData();
        BeanUtils.copyProperties(data, hookData);
        // 通知所有商户
        for (Long shopId : shopIdList) {
            // 加锁执行
            String lock = LockKeyProduce.produce(LockServiceType.SHOP_CARD_BIN, shopId, hookData.getId());
            redissonLockExecutor.execute(lock, () -> {
                // 更新商户此卡头的状态
                LambdaUpdateWrapper<ShopCardBin> uw = new LambdaUpdateWrapper<>();
                uw.eq(ShopCardBin::getShopId, shopId)
                        .eq(ShopCardBin::getCardBinId, data.getId())
                        .set(ShopCardBin::getStatus, data.getStatus())
                        .set(ShopCardBin::getMaintain, data.getMaintain())
                        .set(ShopCardBin::getUpdateTime, LocalDateTime.now());
                if (baseMapper.update(uw) != 1) {
                    throw new BizException("商户卡头更新失败");
                }
            });
            // 查询商户
            ShopInfo shopInfo = shopService.getShopInfoById(shopId);
            if (shopInfo == null) {
                log.warn("vs卡头状态变更 - 商户ID={}, 卡头ID={}, 商户不存在", shopId, data.getId());
                continue;
            }
            // 回调商户
            shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.CARD_BIN_STATUS, hookData);
        }
    }

    @Override
    public ShopCardBin checkCardBin(Long shopId, Long cardBinId) {
        // 校验卡头是否存在
        LambdaQueryWrapper<ShopCardBin> qw = new LambdaQueryWrapper<>();
        qw.eq(ShopCardBin::getShopId, shopId)
                .eq(ShopCardBin::getCardBinId, cardBinId);
        ShopCardBin shopCardBin = baseMapper.selectOne(qw);
        if (shopCardBin == null) {
            throw new BizException("商户不存在此卡头");
        }
        return shopCardBin;
    }

    private ShopInfo checkShop(Long shopId) {
        // 校验商户是否存在
        ShopInfo shopInfo = shopService.getShopInfoById(shopId);
        if (shopInfo == null) {
            throw new BizException("商户不存在");
        }
        return shopInfo;
    }
}
