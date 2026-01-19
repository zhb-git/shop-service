package com.shop_service.web.controller.shop;

import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.request.ShopCardFundDetailPageQuery;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.model.response.ShopCardFundDetailVo;
import com.shop_service.service.IShopCardFundDetailService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户卡片资金明细接口
 *
 * @author 啊祖
 * @date 2026-01-14 22:03
 **/
@RestController
@RequestMapping("/shop/cardFundDetail")
public class ShopCardFundDetailController {
    @Resource
    private IShopCardFundDetailService shopCardFundDetailService;

    /**
     * 分页查询卡片资金明细
     * @param shopInfo 商户信息
     * @param query    参数
     * @return 响应
     */
    @GetMapping("/getPage")
    R<RespPage<ShopCardFundDetailVo>> getPage(@RequestAttribute ShopInfo shopInfo, @Valid ShopCardFundDetailPageQuery query) {
        RespPage<ShopCardFundDetailVo> page = shopCardFundDetailService.getShopCardFundDetailVoPage(shopInfo, query);
        return R.success(page);
    }
}
