package com.shop_service.config;

import com.shop_service.service.IShopService;
import com.shop_service.web.filter.AdminTokenFilter;
import com.shop_service.web.filter.ShopTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security配置
 *
 * @author 啊祖
 * @date 2026-01-12 12:53
 **/
@Configuration
public class SecurityConfig {
    /**
     * 禁用Basic Auth
     * @return bean
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("UserDetailsService is disabled");
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SystemConfig systemConfig, IShopService shopService) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // admin拦截
                .addFilterBefore(new AdminTokenFilter(systemConfig), UsernamePasswordAuthenticationFilter.class)
                // 商户拦截
                .addFilterBefore(new ShopTokenFilter(shopService), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/clientServe/**").hasRole("CLIENT_SERVE")
                        .anyRequest().permitAll()
                );
        return http.build();
    }
}
