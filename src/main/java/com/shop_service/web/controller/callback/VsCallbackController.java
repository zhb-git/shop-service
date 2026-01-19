package com.shop_service.web.controller.callback;

import com.shop_service.biz.VsCallbackServiceBiz;
import com.shop_service.model.request.VsCallbackQuery;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * vs回调
 *
 * @author 啊祖
 * @date 2026-01-15 14:40
 **/
@Slf4j
@RestController
@RequestMapping("/callback/vs")
public class VsCallbackController {
    @Resource
    private VsCallbackServiceBiz vsCallbackServiceBiz;

    /**
     * vs回调接口
     * @param query 参数
     * @return 响应
     */
    @PostMapping("/webhook")
    String webhook(@RequestBody @Valid VsCallbackQuery query) {
        log.debug("vs回调内容: {}", query);
        try {
            vsCallbackServiceBiz.execute(query);
        } catch (Exception e) {
            log.error("vs回调处理异常", e);
        }
        // 必须返回success否则会重复回调
        return "success";
    }
}
