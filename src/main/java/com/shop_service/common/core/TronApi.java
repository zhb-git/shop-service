package com.shop_service.common.core;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.shop_service.config.SystemConfig;
import com.shop_service.config.TronConfig;
import com.shop_service.exception.ApiRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 波场接口
 *
 * @author 啊祖
 * @date 2026-01-14 17:44
 **/
@Slf4j
@Component
public class TronApi {
    // 波场配置
    private final TronConfig tronConfig;
    // 请求头
    private final Map<String, Object> header = new HashMap<>();

    public TronApi(SystemConfig systemConfig, TronConfig tronConfig) {
        this.tronConfig = tronConfig;
        header.put("ShopId", tronConfig.getId());
        header.put("ShopApikey", tronConfig.getKey());
        // 设置地址监控回调
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> set = new HashMap<>();
        set.put("addressControlCallbackUrl", systemConfig.getDomain() + "/callback/tronAddressControl");
        param.put("set", set);
        String api = tronConfig.getApi() + "/shop/set/update";
        request(api, param);
        log.info("波场服务商户设置成功");
    }

    /**
     * 创建监控地址
     * @param address  监控地址
     * @param callback 回调数据
     */
    public void createAddressControl(String address, String callback) {
        Map<String, Object> param = new HashMap<>();
        param.put("address", address);
        param.put("callbackData", callback);
        String api = tronConfig.getApi() + "/shop/address-control/create";
        JSONObject json = request(api, param);
        log.info("波场服务创建监控地址成功 - address={}, response: {}", address, json);
    }

    private JSONObject request(String api, Map<String, Object> param) {
        try {
            HttpRequest request = HttpRequest.post(api)
                    .body(JSON.toJSONString(param))
                    .timeout(10_1000);
            header.forEach((k, v) -> request.header(k, String.valueOf(v)));
            try (HttpResponse response = request.execute()) {
                String body = response.body();
                if (body == null) {
                    throw new ApiRequestException("波场接口请求失败");
                }
                JSONObject json = JSON.parseObject(body);
                Integer code = json.getInteger("code");
                if (code != 200) {
                    throw new ApiRequestException("波场接口请求失败, 原因: " + json.getString("message"));
                }
                return json.getJSONObject("data");
            }
        } catch (Exception e) {
            log.error("波场接口请求失败 - url: {}, error: {}", api, e.getMessage());
            throw new ApiRequestException("波场接口请求失败: " + e.getMessage(), e);
        }
    }
}
