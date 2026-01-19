package com.shop_service.web.chrome;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * @className: ChromeDevtoolsController
 * @author: Java之父
 * @date: 2025/10/29 20:27
 * @version: 1.0.0
 * @description: 谷歌浏览器请求控制器
 */
@Controller
public class ChromeDevtoolsController {
    @GetMapping("/.well-known/appspecific/com.chrome.devtools.json")
    @ResponseBody
    public Map<String, Object> ignoreChrome() {
        return Map.of("ok", true);
    }
}
