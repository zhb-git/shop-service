package com.shop_service.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import org.springframework.util.StringUtils;

/**
 * mybatis-plus公共查询参数填充工具类
 *
 * @author 啊祖
 * @date 2026-01-16 16:11
 **/
public class MpQueryFill {
    /**
     * 填充 cardId/cardNo 的模糊查询条件
     *
     * @param wrapper     查询 wrapper
     * @param cardId      卡片ID(模糊)
     * @param cardNo      卡号(模糊)
     * @param cardIdField cardId 字段方法引用, 例如 ShopCard::getCardId
     * @param cardNoField cardNo 字段方法引用, 例如 ShopCard::getCardNo
     */
    public static <T> void fillCardLike(LambdaQueryWrapper<T> wrapper,
                                        String cardId,
                                        String cardNo,
                                        SFunction<T, ?> cardIdField,
                                        SFunction<T, ?> cardNoField) {
        if (wrapper == null) return;

        if (StringUtils.hasText(cardId) && cardIdField != null) {
            wrapper.like(cardIdField, cardId.trim());
        }
        if (StringUtils.hasText(cardNo) && cardNoField != null) {
            wrapper.like(cardNoField, cardNo.trim());
        }
    }

    /**
     * 可选: 如果你想固定用 likeRight(前缀匹配), 可以用这个
     */
    public static <T> void fillCardLikeRight(LambdaQueryWrapper<T> wrapper,
                                             String cardId,
                                             String cardNo,
                                             SFunction<T, ?> cardIdField,
                                             SFunction<T, ?> cardNoField) {
        if (wrapper == null) return;

        if (StringUtils.hasText(cardId) && cardIdField != null) {
            wrapper.likeRight(cardIdField, cardId.trim());
        }
        if (StringUtils.hasText(cardNo) && cardNoField != null) {
            wrapper.likeRight(cardNoField, cardNo.trim());
        }
    }

    /**
     * 可选: 商户隔离条件, 防止忘记加 where shop_id = ?
     */
    public static <T> void fillShopIdEq(LambdaQueryWrapper<T> wrapper,
                                        Long shopId,
                                        SFunction<T, ?> shopIdField) {
        if (wrapper == null) return;

        if (shopId != null && shopIdField != null) {
            wrapper.eq(shopIdField, shopId);
        }
    }

    /**
     * 可选: 商户号隔离条件
     */
    public static <T> void fillShopNoEq(LambdaQueryWrapper<T> wrapper,
                                        String shopNo,
                                        SFunction<T, ?> shopNoField) {
        if (wrapper == null) return;

        if (StringUtils.hasText(shopNo) && shopNoField != null) {
            wrapper.eq(shopNoField, shopNo.trim());
        }
    }
}
