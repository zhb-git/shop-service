package com.shop_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * vs接口配置
 *
 * @author 啊祖
 * @date 2026-01-14 22:54
 **/
@Data
@Configuration
@ConfigurationProperties("vs")
public class VsConfig {
    /**
     * vs卡片访问接口
     */
    private String api;

    /**
     * 凭证
     */
    private String token;
}
