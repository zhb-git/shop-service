package com.shop_service.web.controller;

import com.shop_service.model.response.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * 测试接口
 *
 * @author 啊祖
 * @date 2026-01-12 09:30
 **/
@RestController
@RequestMapping("/")
public class IndexController {
    /**
     * 测试接口
     * @return 响应
     */
    @GetMapping
    R<LocalDateTime> index() {
        return R.success(LocalDateTime.now());
    }
}
