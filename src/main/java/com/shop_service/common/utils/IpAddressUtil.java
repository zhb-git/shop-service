package com.shop_service.common.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ip工具类
 *
 * @author 啊祖
 * @date 2025-12-14 14:05
 **/
public class IpAddressUtil {
    /**
     * 获取请求IP
     * @param request 请求
     * @return ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0];
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
