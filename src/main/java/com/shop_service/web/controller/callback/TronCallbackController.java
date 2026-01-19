package com.shop_service.web.controller.callback;

import com.shop_service.common.constant.TronAddressControlType;
import com.shop_service.model.pojo.TronAddressControlTransfer;
import com.shop_service.model.request.TronAddressControlCallbackQuery;
import com.shop_service.service.IShopService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 波场回调接口
 *
 * @author 啊祖
 * @date 2026-01-14 18:31
 **/
@RestController
@RequestMapping("/callback/tron")
public class TronCallbackController {
    @Resource
    private IShopService shopService;

    /**
     * 地址监控回调
     * @param query 参数
     */
    @PostMapping("/addressControl")
    void addressControl(@RequestBody @Valid TronAddressControlCallbackQuery query) {
        // 监控结果类型
        if (TronAddressControlType.TRANSFER.getValue() == query.getType()) {
            // 转账
            TronAddressControlTransfer transfer = query.getData().to(TronAddressControlTransfer.class);
            // 处理商户充值
            shopService.rechargeAddressTransfer(transfer);
        }
    }
}
