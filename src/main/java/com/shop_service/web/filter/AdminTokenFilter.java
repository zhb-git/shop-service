package com.shop_service.web.filter;

import com.shop_service.common.constant.HttpHeader;
import com.shop_service.config.SystemConfig;
import com.shop_service.exception.QueryException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.List;

/**
 * 管理员Token过滤器
 *
 * @author 啊祖
 * @date 2026-01-12 12:50
 **/
public class AdminTokenFilter extends OncePerRequestFilter {
    // 系统凭证
    private final SystemConfig systemConfig;
    // 管理员接口拦截路径
    private final RequestMatcher adminMatcher = PathPatternRequestMatcher.withDefaults().matcher("/admin/**");
    // 监控端点拦截路径
    private final RequestMatcher actuatorMatcher = PathPatternRequestMatcher.withDefaults().matcher("/actuator/**");

    public AdminTokenFilter(SystemConfig systemConfig) {
        this.systemConfig = systemConfig;
    }

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return !(adminMatcher.matches(request) || actuatorMatcher.matches(request));
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        // 放行已认证请求
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 取出凭证
        String token = request.getHeader(HttpHeader.X_TOKEN);
        String expected = systemConfig.getAdminToken();

        // 认证
        if (!StringUtils.hasText(token)) {
            throw new QueryException("缺少认证参数");
        }
        if (!expected.equals(token)) {
            throw new AuthenticationException("凭证错误");
        }
        var authentication = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 放行
        filterChain.doFilter(request, response);
    }
}
