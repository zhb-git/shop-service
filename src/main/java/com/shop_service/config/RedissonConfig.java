package com.shop_service.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * redisson配置类
 *
 * @author 啊祖
 * @date 2026-01-12 09:23
 **/
@Configuration
public class RedissonConfig {
    /**
     * redis地址
     */
    @Value("${redisson.address}")
    private String address;

    /**
     * 数据库
     */
    @Value("${redisson.database:0}")
    private int database;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(database);
        return Redisson.create(config);
    }
}
