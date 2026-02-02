package com.shop_service.web.controller.admin;

import com.shop_service.model.request.AdminShopCardPageQuery;
import com.shop_service.model.response.AdminShopCardVo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopCardService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户卡片管理接口
 *
 * @author 啊祖
 * @date 2026-02-01 18:20
 **/
@RestController
@RequestMapping("/admin/shop/card")
public class AdminShopCardController {
    @Resource
    private IShopCardService shopCardService;

    /**
     * 分页查询
     * @param query 参数
     * @return 响应
     */
    @GetMapping("/getPage")
    R<RespPage<AdminShopCardVo>> getPage(@Valid AdminShopCardPageQuery query) {
        RespPage<AdminShopCardVo> page = shopCardService.getAdminShopCardVoPage(query);
        return R.success(page);
    }
}
