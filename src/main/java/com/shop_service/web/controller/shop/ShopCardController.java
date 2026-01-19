package com.shop_service.web.controller.shop;

import com.shop_service.common.constant.VsCardTransferType;
import com.shop_service.exception.QueryException;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.request.*;
import com.shop_service.model.response.*;
import com.shop_service.service.IShopCardService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商户卡片接口
 *
 * @author 啊祖
 * @date 2026-01-14 21:02
 **/
@RestController
@RequestMapping("/shop/card")
public class ShopCardController {
    @Resource
    private IShopCardService shopCardService;

    /**
     * 开卡
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 响应
     */
    @PostMapping("/open")
    R<String> open(@RequestAttribute ShopInfo shopInfo, @RequestBody @Valid ShopOpenCardQuery query) {
        String txId = shopCardService.openCard(shopInfo, query);
        return R.success(txId);
    }

    /**
     * 查询卡片信息
     * @param shopInfo 商户信息
     * @param cardId   卡片ID
     * @return 响应
     */
    @GetMapping("/getCardInfo")
    R<ShopCardInfoVo> getCardInfo(@RequestAttribute ShopInfo shopInfo, @RequestParam String cardId) {
        ShopCardInfoVo vo = shopCardService.getCardInfo(shopInfo, cardId);
        return R.success(vo);
    }

    /**
     * 分页查询卡片
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 响应
     */
    @GetMapping("/getPage")
    R<RespPage<ShopCardVo>> getPage(@RequestAttribute ShopInfo shopInfo, @Valid ShopShopCardPageQuery query) {
        RespPage<ShopCardVo> page = shopCardService.getShopCardVoPage(shopInfo, query);
        return R.success(page);
    }

    /**
     * 查询卡片余额
     * @param shopInfo 商户信息
     * @param cardId   卡片ID
     * @return 响应
     */
    @GetMapping("/getBalance")
    R<ShopCardBalanceVo> getBalance(@RequestAttribute ShopInfo shopInfo, @RequestParam String cardId) {
        ShopCardBalanceVo vo = shopCardService.getShopCardBalanceVo(shopInfo, cardId);
        return R.success(vo);
    }

    /**
     * 注销卡片
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 响应
     */
    @PostMapping("/destroy")
    R<String> destroy(@RequestAttribute ShopInfo shopInfo, @RequestBody @Valid ShopDestroyCardQuery query) {
        String txId = shopCardService.destroyCard(shopInfo, query);
        return R.success(txId);
    }

    /**
     * 冻结卡片
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 响应
     */
    @PostMapping("/freeze")
    R<String> freeze(@RequestAttribute ShopInfo shopInfo, @RequestBody @Valid ShopFreezeCardQuery query) {
        String txId = shopCardService.freezeCard(shopInfo, query);
        return R.success(txId);
    }

    /**
     * 解冻卡片
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 响应
     */
    @PostMapping("/unfreeze")
    R<String> unfreeze(@RequestAttribute ShopInfo shopInfo, @RequestBody @Valid ShopUnfreezeCardQuery query) {
        String txId = shopCardService.unfreezeCard(shopInfo, query);
        return R.success(txId);
    }

    /**
     * 卡片转账
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 响应
     */
    @PostMapping("/transfer")
    R<String> transfer(@RequestAttribute ShopInfo shopInfo, @RequestBody @Valid ShopCardTransferQuery query) {
        // 校验转账类型
        VsCardTransferType transferType = VsCardTransferType.fromValue(query.getType());
        if (transferType == null) {
            throw new QueryException("转账类型不存在");
        }
        // 发起转账
        String txId = shopCardService.cardTransfer(shopInfo, query, transferType);
        return R.success(txId);
    }
}
