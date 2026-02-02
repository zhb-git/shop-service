package com.shop_service.biz;

import com.shop_service.common.constant.LockServiceType;
import com.shop_service.common.constant.VsCallbackType;
import com.shop_service.common.core.LockKeyProduce;
import com.shop_service.common.core.RedissonLockExecutor;
import com.shop_service.exception.BizException;
import com.shop_service.model.pojo.*;
import com.shop_service.model.request.VsCallbackQuery;
import com.shop_service.service.IShopCardBinService;
import com.shop_service.service.IShopCardFundDetailService;
import com.shop_service.service.IShopCardService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * vs回调处理业务类
 *
 * @author 啊祖
 * @date 2026-01-15 14:46
 **/
@Slf4j
@Service
public class VsCallbackServiceBiz {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonLockExecutor redissonLockExecutor;

    @Resource
    private IShopCardService shopCardService;

    @Resource
    private IShopCardBinService shopCardBinService;

    @Resource
    private IShopCardFundDetailService shopCardFundDetailService;

    // redis 回调ID 键
    private final String redisVsCallbackId = "VsCallbackId";

    /**
     * 处理回调
     * @param query 参数
     */
    public void execute(VsCallbackQuery query) {
        // 构建锁
        String lock = LockKeyProduce.produce(LockServiceType.VS_CALLBACK, query.getId());
        redissonLockExecutor.execute(lock, () -> {
            // 验证是否重复处理
            String redisKey = String.format("%s:%s", redisVsCallbackId, query.getId());
            Boolean first = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", 1, TimeUnit.DAYS);
            if (Boolean.FALSE.equals(first)) {
                log.info("vs回调重复 id={}", query.getId());
                throw new BizException("回调重复 id=" + query.getId());
            }
            VsCallbackType callbackType = VsCallbackType.fromValue(query.getType());
            try {
                if (VsCallbackType.OPEN_CARD_RESULT.equals(callbackType)) {
                    shopCardService.openCardResultCallback(query.getData().to(VsOpenCardResultCallbackData.class));
                } else if (VsCallbackType.DESTROY_CARD_RESULT.equals(callbackType)) {
                    shopCardService.destroyCardResultCallback(query.getData().to(VsDestroyCardResultCallbackData.class));
                } else if (VsCallbackType.FROZEN_CARD_RESULT.equals(callbackType)) {
                    shopCardService.freezeCardResultCallback(query.getData().to(VsFreezeCardResultCallbackData.class));
                } else if (VsCallbackType.UNFROZEN_CARD_RESULT.equals(callbackType)) {
                    shopCardService.unfreezeCardResultCallback(query.getData().to(VsUnfreezeCardResultCallbackData.class));
                } else if (VsCallbackType.CARD_TRANSFER_RESULT.equals(callbackType)) {
                    shopCardService.cardTransferResultCallback(query.getData().to(VsCardTransferResultCallbackData.class));
                } else if (VsCallbackType.DESTROY_CARD.equals(callbackType)) {
                    shopCardService.destroyCardCallback(query.getData().to(VsCardDestroyCallbackData.class));
                } else if (VsCallbackType.FROZEN_CARD.equals(callbackType)) {
                    shopCardService.freezeCardCallback(query.getData().to(VsFreezeCardCallbackData.class));
                } else if (VsCallbackType.UNFROZEN_CARD.equals(callbackType)) {
                    shopCardService.unfreezeCardCallback(query.getData().to(VsUnfreezeCardCallbackData.class));
                } else if (VsCallbackType.CARD_OVERSPEND.equals(callbackType)) {
                    shopCardService.cardOverspendCallback(query.getData().to(VsCardOverspendCallbackData.class));
                } else if (VsCallbackType.CARD_SETTLEMENT.equals(callbackType)) {
                    shopCardService.cardSettlementCallback(query.getData().to(VsCardSettlementCallbackData.class));
                } else if (VsCallbackType.CARD_BIND_PLATFORM.equals(callbackType)) {
                    shopCardService.cardBindPlatformCallback(query.getData().to(VsCardBindPlatformCallbackData.class));
                } else if (VsCallbackType.CARD_3DS.equals(callbackType)) {
                    shopCardService.card3DSCallback(query.getData().to(VsCard3DSCallbackData.class));
                } else if (VsCallbackType.CARD_BIN_STATUS.equals(callbackType)) {
                    shopCardBinService.cardBinStatusCallback(query.getData().to(VsCardBinStatusCallbackData.class));
                } else if (VsCallbackType.CARD_FUND_DETAIL.equals(callbackType)) {
                    shopCardFundDetailService.cardFundDetailCallback(query.getData().to(VsCardFundDetailCallbackData.class));
                }
            } finally {
                // 回调id写入redis
                stringRedisTemplate.opsForSet().add(redisVsCallbackId, query.getId());
            }
        });
    }
}
