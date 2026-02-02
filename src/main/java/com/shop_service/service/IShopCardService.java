package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.common.constant.VsCardStatus;
import com.shop_service.common.constant.VsCardTransferType;
import com.shop_service.model.entity.ShopCard;
import com.shop_service.model.pojo.*;
import com.shop_service.model.request.*;
import com.shop_service.model.response.*;

import java.time.LocalDate;

/**
 * 商户卡片服务层
 *
 * @author 啊祖
 * @date 2026-01-14 16:29
 **/
public interface IShopCardService extends IService<ShopCard> {
    /**
     * 商户开卡
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 交易ID
     */
    String openCard(ShopInfo shopInfo, ShopOpenCardQuery query);

    /**
     * 开卡结果回调
     * @param data 数据
     */
    void openCardResultCallback(VsOpenCardResultCallbackData data);

    /**
     * 分页查询
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 结果
     */
    RespPage<ShopCardVo> getShopCardVoPage(ShopInfo shopInfo, ShopShopCardPageQuery query);

    /**
     * 查询卡片余额
     * @param shopInfo 商户信息
     * @param cardId   卡片ID
     * @return 结果
     */
    ShopCardBalanceVo getShopCardBalanceVo(ShopInfo shopInfo, String cardId);

    /**
     * 注销卡片
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 结果
     */
    String destroyCard(ShopInfo shopInfo, ShopDestroyCardQuery query);

    /**
     * 注销卡片结果回调
     * @param data 数据
     */
    void destroyCardResultCallback(VsDestroyCardResultCallbackData data);

    /**
     * 卡片注销回调
     * @param data 数据
     */
    void destroyCardCallback(VsCardDestroyCallbackData data);

    /**
     * 冻结卡片
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 交易ID
     */
    String freezeCard(ShopInfo shopInfo, ShopFreezeCardQuery query);

    /**
     * 冻卡结果回调
     * @param data 数据
     */
    void freezeCardResultCallback(VsFreezeCardResultCallbackData data);

    /**
     * 冻卡回调
     * @param data 数据
     */
    void freezeCardCallback(VsFreezeCardCallbackData data);

    /**
     * 解冻卡片
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 交易ID
     */
    String unfreezeCard(ShopInfo shopInfo, ShopUnfreezeCardQuery query);

    /**
     * 解冻卡片结果回调
     * @param data 数据
     */
    void unfreezeCardResultCallback(VsUnfreezeCardResultCallbackData data);

    /**
     * 解冻卡片回调
     * @param data 数据
     */
    void unfreezeCardCallback(VsUnfreezeCardCallbackData data);

    /**
     * 卡片转账
     * @param shopInfo     商户信息
     * @param query        参数
     * @param transferType 转账类型
     * @return 交易ID
     */
    String cardTransfer(ShopInfo shopInfo, ShopCardTransferQuery query, VsCardTransferType transferType);

    /**
     * 卡片转账结果回调
     * @param data 数据
     */
    void cardTransferResultCallback(VsCardTransferResultCallbackData data);

    /**
     * 卡片超支回调
     * @param data 数据
     */
    void cardOverspendCallback(VsCardOverspendCallbackData data);

    /**
     * 卡片超支结算回调
     * @param data 数据
     */
    void cardSettlementCallback(VsCardSettlementCallbackData data);

    /**
     * 卡片绑定平台回调
     * @param data 数据
     */
    void cardBindPlatformCallback(VsCardBindPlatformCallbackData data);

    /**
     * 卡片3DS回调
     * @param data 数据
     */
    void card3DSCallback(VsCard3DSCallbackData data);

    /**
     * 查询卡片
     * @param cardId 卡片ID
     * @return 卡片
     */
    ShopCard getByCardId(String cardId);

    /**
     * 获取卡片信息
     * @param shopInfo 商户信息
     * @param cardId   卡片ID
     * @return 卡片信息
     */
    ShopCardInfoVo getCardInfo(ShopInfo shopInfo, String cardId);

    /**
     * 分页查询
     * @param query 参数
     * @return 结果
     */
    RespPage<AdminShopCardVo> getAdminShopCardVoPage(AdminShopCardPageQuery query);

    /**
     * 获取卡片数量
     * @param status 状态
     * @param date   时间
     * @return 数量
     */
    long getCardSize(VsCardStatus status, LocalDate date);
}
