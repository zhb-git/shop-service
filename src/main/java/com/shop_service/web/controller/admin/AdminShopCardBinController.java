package com.shop_service.web.controller.admin;

import com.shop_service.model.request.AdminDeleteShopCardBinQuery;
import com.shop_service.model.request.AdminSetShopCardBinQuery;
import com.shop_service.model.request.AdminShopCardBinPageQuery;
import com.shop_service.model.request.AdminUpdateShopCardBinOpenAmountQuery;
import com.shop_service.model.response.AdminShopCardBinVo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopCardBinService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商户卡头管理接口
 *
 * @author 啊祖
 * @date 2026-01-14 22:07
 **/
@RestController
@RequestMapping("/admin/shop/cardBin")
public class AdminShopCardBinController {
    @Resource
    private IShopCardBinService shopCardBinService;

    /**
     * 设置商户卡头
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/setShopCardBin")
    R<String> setShopCardBin(@RequestBody @Valid AdminSetShopCardBinQuery query) {
        shopCardBinService.setShopCardBin(query);
        return R.success();
    }

    /**
     * 分页查询商户卡头
     * @param query 参数
     * @return 响应
     */
    @GetMapping("/getPage")
    R<RespPage<AdminShopCardBinVo>> getList(@Valid AdminShopCardBinPageQuery query) {
        RespPage<AdminShopCardBinVo> page = shopCardBinService.getAdminShopCardBinVoList(query);
        return R.success(page);
    }

    /**
     * 更新商户卡头开卡价格
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/updateShopCardBinOpenAmount")
    R<String> updateShopCardBinOpenAmount(@RequestBody @Valid AdminUpdateShopCardBinOpenAmountQuery query) {
        shopCardBinService.updateShopCardBinOpenAmount(query);
        return R.success();
    }

    /**
     * 删除商户卡头
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/deleteShopCardBin")
    R<String> deleteShopCardBin(@RequestBody @Valid AdminDeleteShopCardBinQuery query) {
        shopCardBinService.deleteShopCardBin(query);
        return R.success();
    }
}
