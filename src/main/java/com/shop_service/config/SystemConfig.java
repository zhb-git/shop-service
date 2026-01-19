package com.shop_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 系统配置
 *
 * @author 啊祖
 * @date 2026-01-12 08:28
 **/
@Data
@Configuration
@ConfigurationProperties("system")
public class SystemConfig {
    /**
     * 项目域名
     */
    private String domain;

    /**
     * 管理员token
     */
    private String adminToken;
}
