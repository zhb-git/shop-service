package com.shop_service.web.filter;

import com.shop_service.common.constant.HttpAttribute;
import com.shop_service.common.constant.HttpHeader;
import com.shop_service.common.utils.IpAddressUtil;
import com.shop_service.exception.QueryException;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.service.IShopService;
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

import java.io.IOException;
import java.util.List;

/**
 * 商户Token过滤器
 *
 * @author 啊祖
 * @date 2026-01-14 17:37
 **/
public class ShopTokenFilter  extends OncePerRequestFilter {
    // 下游服务业务类
    private final IShopService shopService;
    // 下游服务接口拦截路径
    private final RequestMatcher matcher = PathPatternRequestMatcher.withDefaults().matcher("/shop/**");

    public ShopTokenFilter(IShopService shopService) {
        this.shopService = shopService;
    }

    @Override
    protected boolean shouldNotFilter(@Nonnull HttpServletRequest request) {
        return !matcher.matches(request);
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        // 放行已认证请求
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取请求IP地址
        String ip = IpAddressUtil.getClientIp(request);

        // 取出凭证
        String shopNo = request.getHeader(HttpHeader.X_SHOP_NO);
        String shopPublicKey = request.getHeader(HttpHeader.X_SHOP_PUBLIC_KEY);
        if (!StringUtils.hasText(shopNo) || !StringUtils.hasText(shopPublicKey)) {
            throw new QueryException("缺少认证参数");
        }

        // 认证
        ShopInfo info = shopService.authentication(ip, shopNo, shopPublicKey);
        var authentication = new UsernamePasswordAuthenticationToken(
                info, null, List.of(new SimpleGrantedAuthority("ROLE_CLIENT_SERVE"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        request.setAttribute(HttpAttribute.SHOP_INFO, info);

        // 放行
        filterChain.doFilter(request, response);
    }
}
