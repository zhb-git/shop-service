package com.shop_service.web.controller.admin;

import com.shop_service.model.request.AdminShopCardFundDetailPageQuery;
import com.shop_service.model.response.AdminShopCardFundDetailVo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopCardFundDetailService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户卡片资金明细管理接口
 *
 * @author 啊祖
 * @date 2026-02-01 18:21
 **/
@RestController
@RequestMapping("/admin/shop/cardFundDetail")
public class AdminShopCardFundDetailController {
    @Resource
    private IShopCardFundDetailService shopCardFundDetailService;

    /**
     * 分页查询
     * @param query 参数
     * @return 响应
     */
    @GetMapping("/getPage")
    R<RespPage<AdminShopCardFundDetailVo>> getPage(@Valid AdminShopCardFundDetailPageQuery query) {
        RespPage<AdminShopCardFundDetailVo> page = shopCardFundDetailService.getAdminShopCardFundDetailVoPage(query);
        return R.success(page);
    }
}
