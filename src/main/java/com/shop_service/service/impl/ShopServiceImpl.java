package com.shop_service.service.impl;

import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop_service.common.constant.*;
import com.shop_service.common.core.LockKeyProduce;
import com.shop_service.common.core.RedissonLockExecutor;
import com.shop_service.common.core.RespPageConvert;
import com.shop_service.common.core.TronApi;
import com.shop_service.common.utils.ShopNoGeneratorUtil;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopMapper;
import com.shop_service.model.entity.Shop;
import com.shop_service.model.entity.ShopFundDetail;
import com.shop_service.model.entity.ShopRechargeRecord;
import com.shop_service.model.pojo.ShopAssetDepositHookPayload;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.pojo.TronAddressControlTransfer;
import com.shop_service.model.request.*;
import com.shop_service.model.response.AdminShopVo;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopVo;
import com.shop_service.service.IShopFundDetailService;
import com.shop_service.service.IShopRechargeRecordService;
import com.shop_service.service.IShopService;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tron.trident.core.key.KeyPair;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 商户服务实现类
 *
 * @author 啊祖
 * @date 2026-01-14 16:28
 **/
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {
    // 安全锁
    private final ReentrantLock lock = new ReentrantLock();
    // redis 商户信息 键
    private final String redisShopInfo = "ShopInfo";

    @Resource
    private RedissonLockExecutor redissonLockExecutor;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IShopFundDetailService shopFundDetailService;

    @Resource
    private IShopRechargeRecordService shopRechargeRecordService;

    @Resource
    private IShopWebhookEventService shopWebhookEventService;

    @Resource
    private TronApi tronApi;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        initRedisShopInfo();
        log.info("初始化所有redis商户信息完成");
    }

    private void initRedisShopInfo() {
        for (Shop shop : baseMapper.selectList(null)) {
            // 写入缓存
            ShopInfo shopInfo = new ShopInfo();
            BeanUtils.copyProperties(shop, shopInfo);
            setRedisShopInfo(shopInfo);
        }
    }

    @Override
    public void create(AdminCreateShopQuery query) {
        lock.lock();
        try {
            Shop shop = new Shop();
            shop.setNo(ShopNoGeneratorUtil.nextShopNo());
            shop.setName(query.getName());
            shop.setPublicKey(UUID.randomUUID().toString());
            shop.setBalance(BigDecimal.valueOf(0.00));
            shop.setIpWhitelist(new ArrayList<>());
            shop.setWebhookSecret(UUID.randomUUID().toString());
            // 默认5s回调超时
            shop.setWebhookTimeoutMs(5000);
            shop.setWebhookEnabled(true);
            // 生成波场充值地址
            KeyPair pair = KeyPair.generate();
            shop.setTronRechargeAddress(pair.toBase58CheckAddress());
            shop.setTronRechargeAddressPrivateKey(pair.toPrivateKey());
            // 默认3%充值费率
            shop.setRechargeFee(BigDecimal.valueOf(3));
            shop.setEnabled(true);
            if (baseMapper.insert(shop) != 1) {
                throw new BizException("商户新增失败");
            }
            // 写入缓存
            ShopInfo shopInfo = new ShopInfo();
            BeanUtils.copyProperties(shop, shopInfo);
            setRedisShopInfo(shopInfo);
            log.info("商户新增成功 - 商户ID={}, 商户号={}, 商户名字={}", shop.getId(), shop.getNo(), shop.getName());
            // 创建波场监控地址 (回调数据为商户ID)
            tronApi.createAddressControl(pair.toBase58CheckAddress(), shop.getNo());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public RespPage<AdminShopVo> getAdminShopVoPage(AdminShopPageQuery query) {
        LambdaQueryWrapper<Shop> wrapper = new LambdaQueryWrapper<>();
        if (query.getShopId() != null) {
            wrapper.eq(Shop::getId, query.getShopId());
        }
        if (StringUtils.hasText(query.getShopNo())) {
            wrapper.like(Shop::getNo, query.getShopNo());
        }
        if (StringUtils.hasText(query.getShopName())) {
            wrapper.like(Shop::getName, query.getShopName());
        }
        Page<Shop> page = baseMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return RespPageConvert.convert(page, AdminShopVo.class);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void update(AdminUpdateShopQuery query) {
        // 生成锁
        String lock = LockKeyProduce.produce(LockServiceType.SHOP, query.getShopId());
        redissonLockExecutor.execute(lock, () -> {
            // 查询商户
            Shop shop = checkShop(query.getShopId());
            // 比较余额
            int compare = query.getBalance().compareTo(shop.getBalance());
            BigDecimal amount = null;
            if (compare > 0) {
                // 余额存入
                amount = query.getBalance().subtract(shop.getBalance());

            } else if (compare < 0) {
                // 扣除余额
                amount = shop.getBalance().subtract(query.getBalance());
                amount = amount.negate();
            }
            BigDecimal beforeBalance = shop.getBalance();
            // 更新商户
            BeanUtils.copyProperties(query, shop);
            if (baseMapper.updateById(shop) != 1) {
                throw new BizException("商户更新失败");
            }
            BigDecimal afterBalance = shop.getBalance();
            // 同步redis
            ShopInfo shopInfo = new ShopInfo();
            BeanUtils.copyProperties(shop, shopInfo);
            setRedisShopInfo(shopInfo);
            log.info("商户更新成功 - 商户ID={}, 商户号={}, 商户名字={}", shop.getId(), shop.getNo(), shop.getName());
            String bizNo = UUID.randomUUID().toString();
            if (amount != null) {
                // 新增资金明细
                ShopFundDetail detail = new ShopFundDetail();
                detail.setShopId(shop.getId());
                detail.setShopNo(shop.getNo());
                ShopFundDetailType detailType = compare > 0 ? ShopFundDetailType.ADMIN_TRANSFER_IN : ShopFundDetailType.ADMIN_TRANSFER_OUT;
                detail.setType(detailType.getValue());
                // 入账正数, 扣款负数
                detail.setAmount(amount);
                detail.setBalanceBefore(beforeBalance);
                detail.setBalanceAfter(afterBalance);
                detail.setBizNo("管理员操作-" + bizNo);
                String remark = String.format("管理员更新商户信息, 原余额: %s, 新余额: %s", beforeBalance, afterBalance);
                detail.setRemark(remark);
                shopFundDetailService.create(detail);
            }
            if (compare > 0) {
                // 新增商户充值记录
                ShopRechargeRecord record = new ShopRechargeRecord();
                record.setShopId(shop.getId());
                record.setShopNo(shop.getNo());
                record.setAmount(amount);
                record.setFeeAmount(BigDecimal.valueOf(0.00));
                record.setDepositAmount(amount);
                record.setManner(ShopRechargeManner.ADMIN.getValue());
                String credential = "管理员更新商户信息操作-" + bizNo;
                record.setCredential(credential);
                shopRechargeRecordService.create(record);
                // 通知商户
                ShopAssetDepositHookPayload payload = new ShopAssetDepositHookPayload();
                payload.setAmount(amount);
                payload.setManner(ShopRechargeManner.ADMIN.getValue());
                payload.setCredential(credential);
                shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.ASSETS_DEPOSIT, payload);
            }
        });
    }

    @Override
    public void resetShopPublicKey(AdminResetShopPublicKeyQuery query) {
        // 生成锁
        String lock = LockKeyProduce.produce(LockServiceType.SHOP, query.getShopId());
        redissonLockExecutor.execute(lock, () -> {
            // 查询商户
            Shop shop = checkShop(query.getShopId());
            // 更新商户
            shop.setPublicKey(UUID.randomUUID().toString());
            if (baseMapper.updateById(shop) != 1) {
                throw new BizException("商户更新失败");
            }
            // 同步redis
            ShopInfo shopInfo = new ShopInfo();
            BeanUtils.copyProperties(shop, shopInfo);
            setRedisShopInfo(shopInfo);
            log.info("商户公钥更新成功 - 商户ID={}, 商户号={}, 商户名字={}", shop.getId(), shop.getNo(), shop.getName());
        });
    }

    @Override
    public void resetShopCallbackSecret(AdminResetCallbackSecretQuery query) {
        // 生成锁
        String lock = LockKeyProduce.produce(LockServiceType.SHOP, query.getShopId());
        redissonLockExecutor.execute(lock, () -> {
            // 查询商户
            Shop shop = checkShop(query.getShopId());
            // 更新商户
            shop.setWebhookSecret(UUID.randomUUID().toString());
            if (baseMapper.updateById(shop) != 1) {
                throw new BizException("商户更新失败");
            }
            // 同步redis
            ShopInfo shopInfo = new ShopInfo();
            BeanUtils.copyProperties(shop, shopInfo);
            setRedisShopInfo(shopInfo);
            log.info("商户回调签名密钥更新成功 - 商户ID={}, 商户号={}, 商户名字={}", shop.getId(), shop.getNo(), shop.getName());
        });
    }

    @Override
    public ShopInfo authentication(String ip, String shopNo, String publicKey) {
        return getRedisShopInfo(shopNo);
    }

    @Override
    public ShopVo getShopVo(Long shopId) {
        Shop shop = baseMapper.selectById(shopId);
        ShopVo shopVo = new ShopVo();
        BeanUtils.copyProperties(shop, shopVo);
        return shopVo;
    }

    @Override
    public ShopInfo getShopInfoById(Long shopId) {
        return getRedisShopInfo(shopId);
    }

    @Override
    public ShopInfo getShopInfoByNo(String shopNo) {
        return getRedisShopInfo(shopNo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void rechargeAddressTransfer(TronAddressControlTransfer transfer) {
        // 只处理USDT充值
        String shopNo = transfer.getCallbackData();
        if (!TronChainCoin.USDT.getValue().equals(transfer.getCoin())) {
            log.warn("商户充值地址出现转账事件 - 商户号={}, 充值{}{}, 非USDT币种", shopNo, transfer.getAmount(), transfer.getCoin());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = getShopInfoByNo(shopNo);
        if (shopInfo == null) {
            log.warn("商户充值地址出现转账事件 - 商户号={}, 充值{}{}, 商户不存在", shopNo, transfer.getAmount(), transfer.getCoin());
            return;
        }
        // 是否是商户转入
        if (TronAddressControlTransferType.TRANSFER_IN.getValue() == transfer.getType()) {
            // 计算手续费
            BigDecimal feeAmount = transfer.getAmount().multiply(shopInfo.getRechargeFee());
            // 计算实际入账
            BigDecimal depositAmount = transfer.getAmount().subtract(feeAmount);
            // 加锁执行
            String lock = LockKeyProduce.produce(LockServiceType.SHOP, shopInfo.getId());
            redissonLockExecutor.execute(lock, () -> {
                // 查询商户
                Shop shop = baseMapper.selectById(shopInfo.getId());
                // 商户入账
                addBalance(shop.getId(), depositAmount, ShopFundDetailType.RECHARGE, transfer.getTransferHash(), "USDT-TRC20充值");
                // 新增商户充值记录
                ShopRechargeRecord record = new ShopRechargeRecord();
                record.setShopId(shop.getId());
                record.setShopNo(shop.getNo());
                record.setAmount(transfer.getAmount());
                record.setFeeAmount(feeAmount);
                record.setDepositAmount(depositAmount);
                record.setManner(ShopRechargeManner.USDT_TRC20.getValue());
                record.setCredential(transfer.getTransferHash());
                shopRechargeRecordService.create(record);
                log.info("商户充值地址出现转账事件 - 商户号={}, 充值{}{}, 手续费={}, 实际入账={}, 成功", shopNo, transfer.getAmount(), transfer.getCoin(), feeAmount, depositAmount);
                // 通知商户
                ShopAssetDepositHookPayload payload = new ShopAssetDepositHookPayload();
                payload.setAmount(depositAmount);
                payload.setManner(ShopRechargeManner.USDT_TRC20.getValue());
                payload.setCredential(transfer.getTransferHash());
                shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.ASSETS_DEPOSIT, payload);
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void addBalance(Long shopId, BigDecimal amount, ShopFundDetailType fundDetailType, String bizNo, String remark) {
        changeBalance(shopId, amount, bizNo, remark, fundDetailType, true);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ChangeBalanceResult subBalance(Long shopId, BigDecimal amount, ShopFundDetailType fundDetailType, String bizNo, String remark) {
        return changeBalance(shopId, amount, bizNo, remark, fundDetailType, false);
    }

    private ChangeBalanceResult changeBalance(Long shopId,
                               BigDecimal amount,
                               String bizNo,
                               String remark,
                               ShopFundDetailType detailType,
                               boolean isAdd) {

        if (shopId == null) throw new BizException("shopId is null");
        if (amount == null) throw new BizException("amount is null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new BizException("amount must be > 0");
        if (bizNo == null || bizNo.isBlank()) throw new BizException("bizNo is blank");

        // 生成锁
        String lock = LockKeyProduce.produce(LockServiceType.SHOP, shopId);

        // 加锁执行
        return redissonLockExecutor.execute(lock, () -> {
            Shop shop = baseMapper.selectById(shopId);
            if (shop == null) throw new BizException("商户不存在, shopId=" + shopId);

            // 操作前余额
            BigDecimal beforeBalance = shop.getBalance() == null ? BigDecimal.ZERO : shop.getBalance();

            // 是否扣款, 余额是否足够
            if (!isAdd && beforeBalance.compareTo(amount) < 0) {
                throw new BizException("商户余额不足");
            }

            BigDecimal delta = isAdd ? amount : amount.negate();
            BigDecimal afterBalance = beforeBalance.add(delta);

            // 只更新余额字段, 避免 updateById 覆盖其他字段
            int updated = baseMapper.update(
                    new LambdaUpdateWrapper<Shop>()
                            .set(Shop::getBalance, afterBalance)
                            .eq(Shop::getId, shopId)
            );
            if (updated != 1) {
                throw new BizException("商户更新失败");
            }

            // 新增资金明细
            ShopFundDetail detail = new ShopFundDetail();
            detail.setShopId(shop.getId());
            detail.setShopNo(shop.getNo());
            detail.setType(detailType.getValue());
            // 入账正数, 扣款负数
            detail.setAmount(delta);
            detail.setBalanceBefore(beforeBalance);
            detail.setBalanceAfter(afterBalance);
            detail.setBizNo(bizNo);
            detail.setRemark(remark);
            shopFundDetailService.create(detail);

            log.info(
                    "商户变更余额, shopId={}, delta={}, before={}, after={}, bizNo={}, remark={}",
                    shopId,
                    delta,
                    beforeBalance,
                    afterBalance,
                    bizNo,
                    remark
            );

            return new ChangeBalanceResult(
                    detail.getId()
            );
        });
    }

    private Shop checkShop(Long shopId) {
        // 查询商户
        Shop shop = baseMapper.selectById(shopId);
        if (shop == null) {
            throw new BizException("商户不存在");
        }
        return shop;
    }

    private void setRedisShopInfo(ShopInfo shopInfo) {
        // 写入redis 商户ID -> 商户信息
        stringRedisTemplate.opsForHash().put(redisShopInfo + ":" + "ID", shopInfo.getId().toString(), JSON.toJSONString(shopInfo));
        // 写入redis 商户号 -> 商户信息
        stringRedisTemplate.opsForHash().put(redisShopInfo + ":" + "NO", shopInfo.getNo(), JSON.toJSONString(shopInfo));
    }

    private ShopInfo getRedisShopInfo(Long shopId) {
        // 查询缓存
        Object data = stringRedisTemplate.opsForHash().get(redisShopInfo + ":" + "ID", shopId.toString());
        if (data == null) {
            return null;
        }
        String json = Convert.toStr(data);
        return JSON.parseObject(json, ShopInfo.class);
    }

    private ShopInfo getRedisShopInfo(String shopNo) {
        // 查询缓存
        Object data = stringRedisTemplate.opsForHash().get(redisShopInfo + ":" + "NO", shopNo);
        if (data == null) {
            return null;
        }
        String json = Convert.toStr(data);
        return JSON.parseObject(json, ShopInfo.class);
    }
}
