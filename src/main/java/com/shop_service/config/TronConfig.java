package com.shop_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 波场接口配置
 *
 * @author 啊祖
 * @date 2026-01-14 17:47
 **/
@Data
@Configuration
@ConfigurationProperties("tron")
public class TronConfig {
    /**
     * 波场访问接口
     */
    private String api;

    /**
     * 商户ID
     */
    private String id;

    /**
     * 商户密钥
     */
    private String key;
}
