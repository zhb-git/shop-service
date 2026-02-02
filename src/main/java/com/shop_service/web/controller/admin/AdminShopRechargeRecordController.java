package com.shop_service.web.controller.admin;

import com.shop_service.model.request.AdminShopRechargeRecordPageQuery;
import com.shop_service.model.response.AdminShopRechargeRecordVo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopRechargeRecordService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商户充值记录管理接口
 *
 * @author 啊祖
 * @date 2026-02-01 18:23
 **/
@RestController
@RequestMapping("/admin/shop/rechargeRecord")
public class AdminShopRechargeRecordController {
    @Resource
    private IShopRechargeRecordService shopRechargeRecordService;

    /**
     * 分页查询
     * @param query 参数
     * @return 响应
     */
    @GetMapping("/getPage")
    R<RespPage<AdminShopRechargeRecordVo>> getPage(@Valid AdminShopRechargeRecordPageQuery query) {
        RespPage<AdminShopRechargeRecordVo> page = shopRechargeRecordService.getAdminShopRechargeRecordVoPage(query);
        return R.success(page);
    }
}
