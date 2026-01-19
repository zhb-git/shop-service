package com.shop_service.web.controller.admin;

import com.shop_service.model.request.*;
import com.shop_service.model.response.AdminShopVo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商户管理接口
 *
 * @author 啊祖
 * @date 2026-01-14 16:48
 **/
@RestController
@RequestMapping("/admin/shop")
public class AdminShopController {
    @Resource
    private IShopService shopService;

    /**
     * 创建商户
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/create")
    R<String> create(@RequestBody @Valid AdminCreateShopQuery query) {
        shopService.create(query);
        return R.success();
    }

    /**
     * 分页查询商户
     * @param query 参数
     * @return 响应
     */
    @GetMapping("/getPage")
    R<RespPage<AdminShopVo>> getPage(@Valid AdminShopPageQuery query) {
        RespPage<AdminShopVo> page = shopService.getAdminShopVoPage(query);
        return R.success(page);
    }

    /**
     * 更新商户
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/update")
    R<String> update(@RequestBody @Valid AdminUpdateShopQuery query) {
        shopService.update(query);
        return R.success();
    }

    /**
     * 重置商户公钥
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/resetShopPublicKey")
    R<String> resetShopPublicKey(@RequestBody @Valid AdminResetShopPublicKeyQuery query) {
        shopService.resetShopPublicKey(query);
        return R.success();
    }

    /**
     * 重置商户回调签名密钥
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/resetShopCallbackSecret")
    R<String> resetShopCallbackSecret(@RequestBody @Valid AdminResetCallbackSecretQuery query) {
        shopService.resetShopCallbackSecret(query);
        return R.success();
    }
}
