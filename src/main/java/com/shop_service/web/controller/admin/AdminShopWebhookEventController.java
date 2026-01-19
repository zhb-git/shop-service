package com.shop_service.web.controller.admin;

import com.shop_service.model.request.AdminShopWebhookEventPageQuery;
import com.shop_service.model.request.AdminShopWebhookEventRetryAllFailQuery;
import com.shop_service.model.request.AdminShopWebhookEventRetryQuery;
import com.shop_service.model.response.AdminShopWebhookEventVo;
import com.shop_service.model.response.R;
import com.shop_service.model.response.RespPage;
import com.shop_service.service.IShopWebhookEventService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商户回调事件管理接口
 *
 * @author 啊祖
 * @date 2026-01-16 20:08
 **/
@RestController
@RequestMapping("/admin/shop/webhookEvent")
public class AdminShopWebhookEventController {
    @Resource
    private IShopWebhookEventService shopWebhookEventService;

    /**
     * 分页查询回调事件
     * @param query 参数
     * @return 结果
     */
    @GetMapping("/getPage")
    R<RespPage<AdminShopWebhookEventVo>> getPage(@Valid AdminShopWebhookEventPageQuery query) {
        RespPage<AdminShopWebhookEventVo> page = shopWebhookEventService.getAdminShopWebhookEventVoPage(query);
        return R.success(page);
    }

    /**
     * 重试回调
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/retry")
    R<String> retry(@RequestBody @Valid AdminShopWebhookEventRetryQuery query) {
        shopWebhookEventService.retry(query.getIdList().toArray(Long[]::new));
        return R.success();
    }

    /**
     * 重试商户所有失败的回调事件
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/retryAllFail")
    R<String> retryAllFail(@RequestBody @Valid AdminShopWebhookEventRetryAllFailQuery query) {
        shopWebhookEventService.retryAllFail(query.getShopId());
        return R.success();
    }
}
