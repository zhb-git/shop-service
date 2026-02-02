package com.shop_service.web.controller.admin;

import com.shop_service.model.request.AdminShopFundDetailPageQuery;
import com.shop_service.model.response.AdminShopFundDetailVo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopFundDetailService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户资金明细管理接口
 *
 * @author 啊祖
 * @date 2026-02-01 18:22
 **/
@RestController
@RequestMapping("/admin/shop/fundDetail")
public class AdminShopFundDetailController {
    @Resource
    private IShopFundDetailService shopFundDetailService;

    /**
     * 分页查询
     * @param query 参数
     * @return 结果
     */
    @GetMapping("/getPage")
    R<RespPage<AdminShopFundDetailVo>> getPage(@Valid AdminShopFundDetailPageQuery query) {
        RespPage<AdminShopFundDetailVo> page = shopFundDetailService.getAdminShopFundDetailVoPage(query);
        return R.success(page);
    }
}
