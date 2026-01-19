package com.shop_service.web.controller.shop;

import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.ShopCardBinVo;
import com.shop_service.service.IShopCardBinService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商户卡头接口
 *
 * @author 啊祖
 * @date 2026-01-14 22:02
 **/
@RestController
@RequestMapping("/shop/cardBin")
public class ShopCardBinController {
    @Resource
    private IShopCardBinService shopCardBinService;

    /**
     * 获取可用卡头列表
     * @param shopInfo 商户信息
     * @return 响应
     */
    @GetMapping("/getList")
    R<List<ShopCardBinVo>> getList(@RequestAttribute ShopInfo shopInfo) {
        List<ShopCardBinVo> voList = shopCardBinService.getShopCardBinVoList(shopInfo.getId());
        return R.success(voList);
    }
}
