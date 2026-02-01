package com.shop_service.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shop_service.common.constant.ShopWebhookEventType;
import com.shop_service.common.constant.VsCardFundDetailType;
import com.shop_service.common.core.RespPageConvert;
import com.shop_service.common.utils.MpQueryFill;
import com.shop_service.exception.BizException;
import com.shop_service.mapper.ShopCardFundDetailMapper;
import com.shop_service.model.entity.ShopCard;
import com.shop_service.model.entity.ShopCardFundDetail;
import com.shop_service.model.pojo.ShopCardFundDetailHookData;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.pojo.VsCardFundDetailCallbackData;
import com.shop_service.model.request.ShopCardFundDetailPageQuery;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopCardFundDetailVo;
import com.shop_service.service.IShopCardFundDetailService;
import com.shop_service.service.IShopCardService;
import com.shop_service.service.IShopService;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * 商户卡片资金明细服务实现类
 *
 * @author 啊祖
 * @date 2026-01-14 16:33
 **/
@Slf4j
@Service
public class ShopCardFundDetailServiceImpl extends ServiceImpl<ShopCardFundDetailMapper, ShopCardFundDetail> implements IShopCardFundDetailService {
    @Resource
    private IShopWebhookEventService shopWebhookEventService;

    @Resource
    private IShopCardService shopCardService;

    @Resource
    private IShopService shopService;

    @Override
    public void cardFundDetailCallback(VsCardFundDetailCallbackData data) {
        // 查询卡片
        ShopCard card = shopCardService.getByCardId(data.getCardId());
        if (card == null) {
            log.warn("vs卡片资金明细回调 - 卡片ID={}, 卡片不存在", data.getCardId());
            return;
        }
        // 查询商户
        ShopInfo shopInfo = shopService.getShopInfoById(card.getShopId());
        if (shopInfo == null) {
            log.warn("vs卡片资金明细回调 - 商户ID={}, 卡片ID={}, 商户不存在", card.getShopId(), data.getCardId());
            return;
        }
        VsCardFundDetailType transactionType = VsCardFundDetailType.fromValue(data.getType());
        log.info(
                "vs卡片资金明细回调 - 商户ID={}, 卡片ID={}, 交易类型={}, 实际卡内扣款/入账={}, 交易详情={}",
                card.getShopId(),
                data.getCardId(),
                transactionType.getDescription(),
                data.getAmount(),
                data.getDetail()
        );
        // 添加卡片资金明细
        ShopCardFundDetail detail = new ShopCardFundDetail();
        BeanUtils.copyProperties(data,detail);
        // 填充商户信息
        detail.setShopId(card.getShopId());
        detail.setShopNo(card.getShopNo());
        if (baseMapper.insert(detail) != 1) {
            throw new BizException("卡片资金明细添加失败");
        }
        // 通知商户
        ShopCardFundDetailHookData hookData = new ShopCardFundDetailHookData();
        BeanUtils.copyProperties(data,hookData);
        shopWebhookEventService.publish(shopInfo, ShopWebhookEventType.CARD_FUND_DETAIL, hookData);
    }

    @Override
    public RespPage<ShopCardFundDetailVo> getShopCardFundDetailVoPage(ShopInfo shopInfo, ShopCardFundDetailPageQuery query) {
        // 构建查询条件
        LambdaQueryWrapper<ShopCardFundDetail> wrapper = new LambdaQueryWrapper<>();
        MpQueryFill.fillCardLike(
                wrapper,
                query.getCardId(),
                query.getCardNo(),
                ShopCardFundDetail::getCardId,
                ShopCardFundDetail::getCardNo
        );
        wrapper.eq(ShopCardFundDetail::getShopId, shopInfo.getId())
                .orderByDesc(ShopCardFundDetail::getCreateTime)
                .orderByDesc(ShopCardFundDetail::getId);
        Page<ShopCardFundDetail> page = baseMapper.selectPage(new Page<>(query.getPageNum(), query.getPageSize()), wrapper);
        return RespPageConvert.convert(page, ShopCardFundDetailVo.class);
    }
}
