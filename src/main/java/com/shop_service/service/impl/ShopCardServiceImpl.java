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
import com.shop_service.common.core.VsApi;
import com.shop_service.common.utils.MpQueryFill;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopCardMapper;
import com.shop_service.model.entity.ShopCard;
import com.shop_service.model.entity.ShopCardBin;
import com.shop_service.model.pojo.*;
import com.shop_service.model.request.*;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopCardBalanceVo;
import com.shop_service.model.response.ShopCardInfoVo;
import com.shop_service.model.response.ShopCardVo;
import com.shop_service.service.IShopCardBinService;
import com.shop_service.service.IShopCardService;
import com.shop_service.service.IShopService;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 商户卡片服务实现类
 *
 * @author 啊祖
 * @date 2026-01-14 16:30
 **/
@Slf4j
@Service
public class ShopCardServiceImpl extends ServiceImpl<ShopCardMapper, ShopCard> implements IShopCardService {
    @Resource
    private IShopService shopService;

    @Resource
    private IShopCardBinService shopCardBinService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private IShopWebhookEventService shopWebhookEventService;

    @Resource
    private RedissonLockExecutor redissonLockExecutor;

    @Resource(name = "applicationTaskExecutor")
    private TaskExecutor taskExecutor;

    @Resource
    private VsApi vsApi;

    // redis 开卡订单 键
    private final String redisOpenCardOrder = "OpenCardOrder";
    // redis 销卡订单 键
    private final String redisDestroyCardOrder = "DestroyCardOrder";
    // redis 冻卡订单 键
    private final String redisFreezeCardOrder = "FreezeCardOrder";
    // redis 解冻卡片订单 键
    private final String redisUnfreezeCardOrder = "UnfreezeCardOrder";
    // redis 卡片转账订单 键
    private final String redisCardTransferOrder = "CardTransferOrder";

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // 异步初始化同步卡片状态
        Thread initSyncCardStatusThread = new Thread(this::initSyncCardStatus);
        initSyncCardStatusThread.setName("initSyncCardStatus");
        initSyncCardStatusThread.start();
    }

    private void initSyncCardStatus() {
        List<CardInfo> cardInfoList = baseMapper.selectNotDestroyCardInfoList();
        if (cardInfoList == null || cardInfoList.isEmpty()) {
            log.info("异步初始化同步卡片状态完成, total=0");
            return;
        }

        // 并发5次
        int batchSize = 5;
        // 失败ID
        Queue<Long> failedIdList = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < cardInfoList.size(); i += batchSize) {
            List<CardInfo> batch = cardInfoList.subList(i, Math.min(i + batchSize, cardInfoList.size()));

            List<CompletableFuture<Void>> futures = batch.stream()
                    .map(info -> CompletableFuture.runAsync(() -> syncOneCardStatus(info.getId(), info.getCardId(), failedIdList), taskExecutor))
                    .toList();

            // 等这一批 5 个全部跑完再进行下一批
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        }

        if (failedIdList.isEmpty()) {
            log.info("异步初始化同步卡片状态完成, total={}", cardInfoList.size());
        } else {
            log.warn(
                    "异步初始化同步卡片状态完成, total={}, failedCount={}",
                    cardInfoList.size(),
                    failedIdList.size()
            );
        }
    }

    private void syncOneCardStatus(Long id, String cardId, Queue<Long> failedIdList) {
        try {
            // 先查卡片信息, 不要在锁里做远程调用, 避免锁持有太久
            VsCardInfo cardInfo = vsApi.getCardInfo(cardId);
            if (cardInfo == null) {
                failedIdList.add(id);
                log.error("异步初始化同步卡片状态失败, 下游返回空, id={}", id);
                return;
            }
            // 加锁执行
            String lock = LockKeyProduce.produce(LockServiceType.SHOP_CARD, id);
            redissonLockExecutor.execute(lock, () -> {
                ShopCard latest = baseMapper.selectById(id);
                if (latest == null) {
                    return;
                }

                if (!Objects.equals(latest.getStatus(), cardInfo.getStatus())) {
                    latest.setStatus(cardInfo.getStatus());
                    if (baseMapper.updateById(latest) != 1) {
                        throw new BizException("商户卡片(id=" + id + ")状态(" + cardInfo.getStatus() + ")更新失败");
                    }
                }
            });
        } catch (Exception e) {
            failedIdList.add(id);
            log.error("异步初始化同步卡片状态异常, id={}, message={}", id, e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String openCard(ShopInfo shopInfo, ShopOpenCardQuery query) {
        // 校验卡头是否存在
        ShopCardBin bin = checkCardBin(shopInfo.getId(), query.getBinId());
        // 校验状态
        if (bin.getMaintain()) {
            throw new BizException("此卡段正在维护");
        }
        if (!bin.getStatus()) {
            throw new BizException("此卡段不可使用");
        }
        // 校验预留金额是否小于最低标准
        if (query.getReserveAmount().compareTo(bin.getCreateMinAmount()) < 0) {
            throw new BizException("此卡段预留金额不能小于" + bin.getCreateMinAmount());
        }
        // 校验预留金额是否大于最高标准
        if (bin.getCreateMaxAmount().compareTo(BigDecimal.ZERO) > 0 &&
                bin.getCreateMaxAmount().compareTo(query.getReserveAmount()) < 0) {
            throw new BizException("此卡段预留金额不能大于" + bin.getCreateMinAmount());
        }
        // 计算扣款金额
        BigDecimal subAmount = query.getReserveAmount().add(bin.getCreateAmount());
        // 商户扣款
        String remark = String.format("开卡扣款, 卡头ID=%s", query.getBinId());
        shopService.subBalance(shopInfo.getId(), subAmount, ShopFundDetailType.OPEN_CARD_SUB, "无", remark);
        // 发起开卡
        String txId = vsApi.openCard(bin.getCardBinId(), query.getReserveAmount());
        // 写入redis
        OpenCardOrder order = new OpenCardOrder();
        order.setShopInfo(shopInfo);
        order.setCardBinId(query.getBinId());
        order.setSpendAmount(subAmount);
        stringRedisTemplate.opsForHash().put(redisOpenCardOrder, txId, JSON.toJSONString(order));
        log.info("vs开卡发起成功 - 商户ID={}, 卡头ID={}, 交易ID={}", shopInfo.getId(), query.getBinId(), txId);
        return txId;
    }

    @Override
    public void openCardResultCallback(VsOpenCardResultCallbackData data) {
        // 查询订单
        Object orderObj = stringRedisTemplate.opsForHash().get(redisOpenCardOrder, data.getTransactionId());
        if (orderObj == null) {
            log.warn("vs开卡结果回调 - 交易ID={}, 开卡订单不存在", data.getTransactionId());
            return;
        }
        try {
            String json = Convert.toStr(orderObj);
            OpenCardOrder order = JSON.parseObject(json, OpenCardOrder.class);
            log.info(
                    "vs开卡结果 - 商户ID={}, 交易ID={}, 是否成功={}",
                    order.getShopInfo().getId(),
                    data.getTransactionId(),
                    data.getSuccess()
            );
            ShopInfo shopInfo = order.getShopInfo();
            VsOpenCardResultCallbackData.CardInfo cardInfo = data.getCardInfo();
            // 是否成功
            if (data.getSuccess()) {
                // 卡片入库
                ShopCard card = new ShopCard();
                BeanUtils.copyProperties(cardInfo, card);
                // 填充商户信息/持卡人地址
                card.setShopId(shopInfo.getId());
                card.setShopNo(shopInfo.getNo());
                ShopCard.HolderAddress holderAddress = new ShopCard.HolderAddress();
                BeanUtils.copyProperties(cardInfo.getHolderAddress(), holderAddress);
                card.setHolderAddress(holderAddress);
                if (baseMapper.insert(card) != 1) {
                    throw new BizException("商户卡片新增失败");
                }
            } else {
                // 商户退款
                String remark = String.format("开卡失败退款, 卡头ID=%s", order.getCardBinId());
                shopService.addBalance(
                        order.getShopInfo().getId(),
                        order.getSpendAmount(),
                        ShopFundDetailType.OPEN_CARD_FAIL_BACK,
                        "开卡交易ID: " + data.getTransactionId(),
                        remark
                );
            }
            // 回调商户
            ShopOpenCardResultHookData hookData = new ShopOpenCardResultHookData();
            BeanUtils.copyProperties(data, hookData);
            // 如果成功则填充卡片信息&&持卡人地址
            if (data.getSuccess()) {
                // 填充卡片信息
                ShopOpenCardResultHookData.CardInfo hookDataCardInfo = new ShopOpenCardResultHookData.CardInfo();
                BeanUtils.copyProperties(data.getCardInfo(), hookDataCardInfo);
                hookData.setCardInfo(hookDataCardInfo);
                // 填充持卡人账单地址信息
                ShopOpenCardResultHookData.CardInfo.HolderAddress holderAddress = new ShopOpenCardResultHookData.CardInfo.HolderAddress();
                BeanUtils.copyProperties(cardInfo.getHolderAddress(), holderAddress);
                hookData.getCardInfo().setHolderAddress(holderAddress);
            }
            shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.OPEN_CARD_RESULT, hookData);
        } finally {
            // 删除redis订单
            stringRedisTemplate.opsForHash().delete(redisOpenCardOrder, data.getTransactionId());
        }
    }

    @Override
    public RespPage<ShopCardVo> getShopCardVoPage(ShopInfo shopInfo, ShopShopCardPageQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<ShopCard> wrapper = new LambdaQueryWrapper<>();
        MpQueryFill.fillCardLike(
                wrapper,
                query.getCardId(),
                query.getCardNo(),
                ShopCard::getCardId,
                ShopCard::getCardNo
        );
        wrapper.eq(ShopCard::getShopId, shopInfo.getId())
                .orderByDesc(ShopCard::getCreateTime)
                .orderByDesc(ShopCard::getId);
        Page<ShopCard> page = baseMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return RespPageConvert.convert(page, ShopCardVo.class);
    }

    @Override
    public ShopCardBalanceVo getShopCardBalanceVo(ShopInfo shopInfo, String cardId) {
        // 校验卡片
        ShopCard card = checkCard(shopInfo.getId(), cardId);
        // 查询余额
        VsCardBalance balance = vsApi.getCardBalance(card.getCardId());
        ShopCardBalanceVo vo = new ShopCardBalanceVo();
        BeanUtils.copyProperties(balance, vo);
        return vo;
    }

    @Override
    public String destroyCard(ShopInfo shopInfo, ShopDestroyCardQuery query) {
        // 校验卡片
        ShopCard card = checkCard(shopInfo.getId(), query.getCardId());
        // 校验卡片状态
        if (VsCardStatus.CANCELLED.getValue() == card.getStatus()) {
            throw new BizException("此卡已注销");
        }
        // 注销卡片
        String txId = vsApi.destroyCard(card.getCardId());
        // 写入redis
        DestroyCardOrder order = new DestroyCardOrder();
        order.setShopInfo(shopInfo);
        order.setId(card.getId());
        stringRedisTemplate.opsForHash().put(redisDestroyCardOrder, txId, JSON.toJSONString(order));

        log.info("vs注销卡片发起成功 - 商户ID={}, 卡片ID={}, 交易ID={}", shopInfo.getId(), query.getCardId(), txId);
        return txId;
    }

    @Override
    public void destroyCardResultCallback(VsDestroyCardResultCallbackData data) {
        // 查询订单
        Object orderObj = stringRedisTemplate.opsForHash().get(redisDestroyCardOrder, data.getTransactionId());
        if (orderObj == null) {
            log.warn("vs销卡结果回调 - 交易ID={}, 销卡订单不存在", data.getTransactionId());
            return;
        }
        try {
            // 序列化
            String json = Convert.toStr(orderObj);
            DestroyCardOrder order = JSON.parseObject(json, DestroyCardOrder.class);
            log.info(
                    "vs销卡结果回调 - 商户ID={}, 卡片ID={}, 交易ID={}, 是否成功={}",
                    order.getShopInfo().getId(),
                    data.getCardId(),
                    data.getTransactionId(),
                    data.getSuccess()
            );
            // 更新卡片
            updateCardStatus(order.getId(), VsCardStatus.CANCELLED);
            // 回调商户
            ShopDestroyCardResultHookData hookData = new ShopDestroyCardResultHookData();
            BeanUtils.copyProperties(data, hookData);
            shopWebhookEventService.publish(order.getShopInfo(), ShopWebhookEventType.DESTROY_CARD_RESULT, hookData);
        } finally {
            // 删除redis订单
            stringRedisTemplate.opsForHash().delete(redisDestroyCardOrder, data.getTransactionId());
        }
    }

    @Override
    public void destroyCardCallback(VsCardDestroyCallbackData data) {
        // 校验卡片是否存在
        ShopCard card = getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片注销回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片注销回调 - 卡片ID={}, 商户ID={}, 商户不存在", data.getCardId(), card.getShopId());
            return;
        }
        log.info("vs销卡回调 - 商户ID={}, 卡片ID={}", shopInfo.getId(), data.getCardId());
        // 更新卡片状态
        updateCardStatus(card.getId(), VsCardStatus.CANCELLED);
        // 异步回调给下游服务
        ShopCardDestroyHookData hookData = new ShopCardDestroyHookData();
        hookData.setCardId(card.getCardId());
        hookData.setCardNo(card.getCardNo());
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.DESTROY_CARD, hookData);
    }

    @Override
    public String freezeCard(ShopInfo shopInfo, ShopFreezeCardQuery query) {
        // 校验卡片
        ShopCard card = checkCard(shopInfo.getId(), query.getCardId());
        // 校验卡片状态
        if (VsCardStatus.NORMAL.getValue() != card.getStatus()) {
            throw new BizException("当前状态不可冻卡");
        }
        // 注销卡片
        String txId = vsApi.freezeCard(card.getCardId());
        // 写入redis
        FreezeCardOrder order = new FreezeCardOrder();
        order.setShopInfo(shopInfo);
        order.setId(card.getId());
        stringRedisTemplate.opsForHash().put(redisFreezeCardOrder, txId, JSON.toJSONString(order));

        log.info("vs冻结卡片发起成功 - 商户ID={}, 卡片ID={}, 交易ID={}", shopInfo.getId(), query.getCardId(), txId);
        return txId;
    }

    @Override
    public void freezeCardResultCallback(VsFreezeCardResultCallbackData data) {
        // 查询订单
        Object orderObj = stringRedisTemplate.opsForHash().get(redisFreezeCardOrder, data.getTransactionId());
        if (orderObj == null) {
            log.warn("vs冻卡结果回调 - 交易ID={}, 销卡订单不存在", data.getTransactionId());
            return;
        }
        try {
            // 序列化
            String json = Convert.toStr(orderObj);
            FreezeCardOrder order = JSON.parseObject(json, FreezeCardOrder.class);
            log.info(
                    "vs冻卡结果回调 - 商户ID={}, 卡片ID={}, 交易ID={}, 是否成功={}",
                    order.getShopInfo().getId(),
                    data.getCardId(),
                    data.getTransactionId(),
                    data.getSuccess()
            );
            // 更新卡片
            updateCardStatus(order.getId(), VsCardStatus.FROZEN);
            // 回调商户
            ShopFreezeCardResultHookData hookData = new ShopFreezeCardResultHookData();
            BeanUtils.copyProperties(data, hookData);
            shopWebhookEventService.publish(order.getShopInfo(), ShopWebhookEventType.FROZEN_CARD_RESULT, hookData);
        } finally {
            // 删除redis订单
            stringRedisTemplate.opsForHash().delete(redisFreezeCardOrder, data.getTransactionId());
        }
    }

    @Override
    public void freezeCardCallback(VsFreezeCardCallbackData data) {
        // 校验卡片是否存在
        ShopCard card = getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片冻结回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片冻结回调 - 卡片ID={}, 商户ID={}, 商户不存在", data.getCardId(), card.getShopId());
            return;
        }
        log.info("vs卡片冻结回调 - 商户ID={}, 卡片ID={}", shopInfo.getId(), data.getCardId());
        // 更新卡片状态
        updateCardStatus(card.getId(), VsCardStatus.FROZEN);
        // 异步回调给下游服务
        ShopFreezeCardHookData hookData = new ShopFreezeCardHookData();
        hookData.setCardId(card.getCardId());
        hookData.setCardNo(card.getCardNo());
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.FROZEN_CARD, hookData);
    }

    @Override
    public String unfreezeCard(ShopInfo shopInfo, ShopUnfreezeCardQuery query) {
        // 校验卡片
        ShopCard card = checkCard(shopInfo.getId(), query.getCardId());
        // 校验状态
        if (VsCardStatus.FROZEN.getValue() != card.getStatus()) {
            throw new BizException("当前状态不可解冻卡片");
        }
        // 注销卡片
        String txId = vsApi.unfreezeCard(card.getCardId());
        // 写入redis
        UnfreezeCardOrder order = new UnfreezeCardOrder();
        order.setShopInfo(shopInfo);
        order.setId(card.getId());
        stringRedisTemplate.opsForHash().put(redisUnfreezeCardOrder, txId, JSON.toJSONString(order));

        log.info("vs解冻结卡片发起成功 - 商户ID={}, 卡片ID={}, 交易ID={}", shopInfo.getId(), query.getCardId(), txId);
        return txId;
    }

    @Override
    public void unfreezeCardResultCallback(VsUnfreezeCardResultCallbackData data) {
        // 查询订单
        Object orderObj = stringRedisTemplate.opsForHash().get(redisUnfreezeCardOrder, data.getTransactionId());
        if (orderObj == null) {
            log.warn("vs解冻卡片结果回调 - 交易ID={}, 解冻卡片订单不存在", data.getTransactionId());
            return;
        }
        try {
            // 序列化
            String json = Convert.toStr(orderObj);
            UnfreezeCardOrder order = JSON.parseObject(json, UnfreezeCardOrder.class);
            log.info(
                    "vs解冻卡片结果回调 - 商户ID={}, 卡片ID={}, 交易ID={}, 是否成功={}",
                    order.getShopInfo().getId(),
                    data.getCardId(),
                    data.getTransactionId(),
                    data.getSuccess()
            );
            // 更新卡片
            updateCardStatus(order.getId(), VsCardStatus.NORMAL);
            // 回调商户
            ShopUnfreezeCardResultHookData hookData = new ShopUnfreezeCardResultHookData();
            BeanUtils.copyProperties(data, hookData);
            shopWebhookEventService.publish(order.getShopInfo(), ShopWebhookEventType.UNFROZEN_CARD_RESULT, hookData);
        } finally {
            // 删除redis订单
            stringRedisTemplate.opsForHash().delete(redisUnfreezeCardOrder, data.getTransactionId());
        }
    }

    @Override
    public void unfreezeCardCallback(VsUnfreezeCardCallbackData data) {
        // 校验卡片是否存在
        ShopCard card = getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片解冻回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片解冻回调 - 卡片ID={}, 商户ID={}, 商户不存在", data.getCardId(), card.getShopId());
            return;
        }
        log.info("vs卡片解冻回调 - 商户ID={}, 卡片ID={}", shopInfo.getId(), data.getCardId());
        // 更新卡片状态
        updateCardStatus(card.getId(), VsCardStatus.NORMAL);
        // 异步回调给下游服务
        ShopUnfreezeCardHookData hookData = new ShopUnfreezeCardHookData();
        hookData.setCardId(card.getCardId());
        hookData.setCardNo(card.getCardNo());
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.UNFROZEN_CARD, hookData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public String cardTransfer(ShopInfo shopInfo, ShopCardTransferQuery query, VsCardTransferType transferType) {
        // 校验卡片
        ShopCard card = checkCard(shopInfo.getId(), query.getCardId());
        // 校验卡片状态
        if (VsCardStatus.NORMAL.getValue() != card.getStatus()) {
            throw new BizException("当前状态不可转账");
        }
        // 校验卡头
        ShopCardBin cardBin = shopCardBinService.checkCardBin(shopInfo.getId(), card.getCardBinId());
        if (VsCardTransferType.TRANSFER_IN.equals(transferType)) {
            // 校验最低转入标准
            if (query.getAmount().compareTo(cardBin.getRechargeMinAmount()) < 0) {
                throw new BizException("此卡段转入金额不能小于" + cardBin.getRechargeMinAmount());
            }
            // 校验最高转入标准
            if (cardBin.getRechargeMaxAmount().compareTo(BigDecimal.ZERO) > 0
                    && query.getAmount().compareTo(cardBin.getRechargeMaxAmount()) > 0) {
                throw new BizException("此卡段转入金额不能大于" + cardBin.getRechargeMaxAmount());
            }
        } else {
            // 校验最低转出标准
            if (query.getAmount().compareTo(cardBin.getWithdrawMinAmount()) < 0) {
                throw new BizException("此卡段转出金额不能小于" + cardBin.getWithdrawMinAmount());
            }
            // 校验最高转出标准
            if (cardBin.getWithdrawMaxAmount().compareTo(BigDecimal.ZERO) > 0 &&
                    query.getAmount().compareTo(cardBin.getWithdrawMaxAmount()) > 0) {
                throw new BizException("此卡段转出金额不能大于" + cardBin.getWithdrawMaxAmount());
            }
        }
        // 转账类型
        if (VsCardTransferType.TRANSFER_IN.equals(transferType)) {
            // 商户扣款
            String remark = String.format("卡片转入, 卡片ID=%s", query.getCardId());
            shopService.subBalance(
                    shopInfo.getId(),
                    query.getAmount(),
                    ShopFundDetailType.CARD_TRANSFER_IN,
                    "无",
                    remark
            );
        } else {
            // 查询卡片余额
            VsCardBalance balance = vsApi.getCardBalance(card.getCardId());
            // 校验卡片余额是否足够转出
            if (balance.getAvailable().compareTo(query.getAmount()) < 0) {
                throw new BizException("卡片余额不足");
            }
        }
        // 发起转账
        String txId = vsApi.cardTransfer(card.getCardId(), transferType, query.getAmount());
        // 写入redis
        CardTransferOrder order = new CardTransferOrder();
        order.setShopInfo(shopInfo);
        stringRedisTemplate.opsForHash().put(redisCardTransferOrder, txId, JSON.toJSONString(order));
        log.info(
                "vs卡片转账发起成功 - 商户ID={}, 卡片ID={}, 交易ID={} {}{}",
                shopInfo.getId(),
                query.getCardId(),
                txId,
                transferType.getDescription(),
                query.getAmount()
        );
        return txId;
    }

    @Override
    public void cardTransferResultCallback(VsCardTransferResultCallbackData data) {
        // 查询订单
        Object orderObj = stringRedisTemplate.opsForHash().get(redisCardTransferOrder, data.getTransactionId());
        if (orderObj == null) {
            log.warn("vs卡片转账结果回调 - 交易ID={}, 卡片转账订单不存在", data.getTransactionId());
            return;
        }
        try {
            // 序列化
            VsCardTransferType transferType = VsCardTransferType.fromValue(data.getTransferType());
            String json = Convert.toStr(orderObj);
            UnfreezeCardOrder order = JSON.parseObject(json, UnfreezeCardOrder.class);
            log.info(
                    "vs卡片转账结果回调 - 商户ID={}, 卡片ID={}, 交易ID={}, {}{}是否成功={}",
                    order.getShopInfo().getId(),
                    data.getCardId(),
                    data.getTransactionId(),
                    data.getSuccess(),
                    transferType.getDescription(),
                    data.getAmount()
            );
            // 是否失败
            if (!data.getSuccess()) {
                // 是否转入失败
                if (VsCardTransferType.TRANSFER_IN.getValue() == data.getTransferType()) {
                    // 商户退款
                    String remark = String.format("卡片转入失败退款, 卡片ID=%s, 金额=%s", data.getCardId(), data.getAmount());
                    shopService.addBalance(
                            order.getShopInfo().getId(),
                            data.getAmount(),
                            ShopFundDetailType.OPEN_CARD_FAIL_BACK,
                            "转账交易ID: " + data.getTransactionId(),
                            remark
                    );
                }
            } else {
                // 是否转出成功
                if (VsCardTransferType.TRANSFER_OUT.getValue() == data.getTransferType()) {
                    // 商户入款
                    String remark = String.format("卡片转出成功, 卡片ID=%s, 金额=%s", data.getCardId(), data.getAmount());
                    shopService.addBalance(
                            order.getShopInfo().getId(),
                            data.getAmount(),
                            ShopFundDetailType.CARD_TRANSFER_OUT,
                            "转账交易ID: " + data.getTransactionId(),
                            remark
                    );
                }
            }
            // 回调商户
            ShopCardTransferResultHookData hookData = new ShopCardTransferResultHookData();
            BeanUtils.copyProperties(data, hookData);
            shopWebhookEventService.publish(order.getShopInfo(), ShopWebhookEventType.CARD_TRANSFER_RESULT, hookData);
        } finally {
            // 删除redis订单
            stringRedisTemplate.opsForHash().delete(redisCardTransferOrder, data.getTransactionId());
        }
    }

    @Override
    public void cardOverspendCallback(VsCardOverspendCallbackData data) {
        // 查询卡片
        ShopCard card = getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片超支回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片超支回调 - 卡片ID={}, 商户ID={}, 商户不存在", data.getCardId(), card.getShopId());
            return;
        }
        log.info("vs卡片超支 - 卡片ID={}, 超支金额={}", data.getCardId(), data.getAmount());
        // 异步回调下游服务
        ShopCardOverspendHookData hookData = new ShopCardOverspendHookData();
        hookData.setCardId(data.getCardId());
        hookData.setCardNo(card.getCardNo());
        hookData.setAmount(data.getAmount());
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.CARD_OVERSPEND, hookData);
    }

    @Override
    public void cardSettlementCallback(VsCardSettlementCallbackData data) {
        // 查询卡片
        ShopCard card = getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片超支结算回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片超支结算回调 - 卡片ID={}, 商户ID={}, 商户不存在", data.getCardId(), card.getShopId());
            return;
        }
        log.info("vs卡片超支结算 - 卡片ID={}, 超支结算金额={}", data.getCardId(), data.getAmount());
        // 异步回调下游服务
        ShopCardSettlementHookData hookData = new ShopCardSettlementHookData();
        hookData.setCardId(data.getCardId());
        hookData.setCardNo(card.getCardNo());
        hookData.setAmount(data.getAmount());
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.CARD_SETTLEMENT, hookData);
    }

    @Override
    public void cardBindPlatformCallback(VsCardBindPlatformCallbackData data) {
        // 查询卡片
        ShopCard card = getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片绑定平台回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片绑定平台回调 - 卡片ID={}, 商户ID={}, 商户不存在", data.getCardId(), card.getShopId());
            return;
        }
        log.info("vs卡片绑定平台 - 卡片ID={}, 平台行业代码={}, 平台详情={}", data.getCardId(), data.getMcc(), data.getDetail());
        // 异步回调下游服务
        ShopCardBindPlatformHookData hookData = new ShopCardBindPlatformHookData();
        BeanUtils.copyProperties(data, hookData);
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.CARD_BIND_PLATFORM, hookData);
    }

    @Override
    public void card3DSCallback(VsCard3DSCallbackData data) {
        // 查询卡片
        ShopCard card = getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片3DS回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户信息
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片3DS回调 - 卡片ID={}, 商户ID={}, 商户不存在", data.getCardId(), card.getShopId());
            return;
        }
        VsCard3DSStage stage = VsCard3DSStage.fromValue(data.getStage());
        log.info("vs卡片3DS - 卡片ID={}, 验证阶段={}", data.getCardId(), stage.getDescription());
        // 异步回调下游服务
        ShopCard3DSHookData hookData = new ShopCard3DSHookData();
        BeanUtils.copyProperties(data, hookData);
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.CARD_3DS, hookData);
    }

    private ShopCard checkCard(Long shopId, String cardId) {
        LambdaQueryWrapper<ShopCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopCard::getShopId, shopId)
                .eq(ShopCard::getCardId, cardId);
        ShopCard shopCard = baseMapper.selectOne(wrapper);
        if (shopCard == null) {
            throw new BizException("卡片不存在");
        }
        return shopCard;
    }

    public ShopCard getByCardId(String cardId) {
        LambdaQueryWrapper<ShopCard> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopCard::getCardId, cardId);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public ShopCardInfoVo getCardInfo(ShopInfo shopInfo, String cardId) {
        ShopCard card = checkCard(shopInfo.getId(), cardId);
        ShopCardInfoVo vo = new ShopCardInfoVo();
        BeanUtils.copyProperties(card, vo);
        return vo;
    }

    private ShopCardBin checkCardBin(Long shopId, Long cardBinId) {
        ShopCardBin cardBin = shopCardBinService.getByShopIdAndCardBinId(shopId, cardBinId);
        if (cardBin == null) {
            throw new BizException("卡头不存在");
        }
        return cardBin;
    }

    private void updateCardStatus(Long id, VsCardStatus cardStatus) {
        int status = cardStatus.getValue();
        String description = cardStatus.getDescription();
        String lock = LockKeyProduce.produce(LockServiceType.SHOP_CARD, id);
        redissonLockExecutor.execute(lock, () -> {
            LambdaUpdateWrapper<ShopCard> wrapper = new LambdaUpdateWrapper<>();
            wrapper.set(ShopCard::getStatus, status)
                    .set(ShopCard::getUpdateTime, LocalDateTime.now())
                    .eq(ShopCard::getId, id);
            if (baseMapper.update(wrapper) != 1) {
                throw new BizException(
                        "vs卡片(id: " + id + ")状态(status: " + status + ", description: " + description + ")更新失败"
                );
            }
        });
    }
}
