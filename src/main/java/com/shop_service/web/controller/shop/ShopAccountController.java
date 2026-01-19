package com.shop_service.web.controller.shop;

import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.ShopVo;
import com.shop_service.service.IShopService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户账户接口
 *
 * @author 啊祖
 * @date 2026-01-14 17:34
 **/
@RestController
@RequestMapping("/shop/account")
public class ShopAccountController {
    @Resource
    private IShopService shopService;

    /**
     * 获取商户信息
     * @param shopInfo 商户信息
     * @return 响应
     */
    @GetMapping("/getInfo")
    R<ShopVo> getInfo(@RequestAttribute ShopInfo shopInfo) {
        ShopVo shopVo = shopService.getShopVo(shopInfo.getId());
        return R.success(shopVo);
    }
}
