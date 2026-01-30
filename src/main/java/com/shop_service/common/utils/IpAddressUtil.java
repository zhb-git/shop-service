package com.shop_service.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 企业级 IP 工具类
 * 支持多层代理识别、IPv6、以及本地回环地址解析
 *
 * @author Gemini
 * @date 2026-01-20
 **/
public class IpAddressUtil {
    // 当无法获取有效 IP 时的缺省标识符(不区分大小写)
    private static final String UNKNOWN = "unknown";
    // IPv4 协议下的本地回环地址
    private static final String LOCALHOST_IPV4 = "127.0.0.1";
    // IPv6 协议下的本地回环地址(在某些系统环境下，本地访问会返回此格式)
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    // HTTP 请求头(如 X-Forwarded-For)中多个 IP 之间的分隔符
    private static final String SEPARATOR = ",";

    /**
     * 获取客户端真实 IP
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }

        // 1. 按照常用代理请求头优先级排序
        String ip = request.getHeader("X-Forwarded-For");
        if (isIpInvalid(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (isIpInvalid(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (isIpInvalid(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (isIpInvalid(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (isIpInvalid(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        // 2. 如果以上都没有，则获取直接连接的远程地址
        if (isIpInvalid(ip)) {
            ip = request.getRemoteAddr();
        }

        // 3. 处理本地回环地址 (127.0.0.1 或 0:0:0:0:0:0:0:1)
        if (LOCALHOST_IPV4.equals(ip) || LOCALHOST_IPV6.equals(ip)) {
            try {
                // 根据网卡获取本机配置的真实 IP
                InetAddress inet = InetAddress.getLocalHost();
                ip = inet.getHostAddress();
            } catch (UnknownHostException e) {
                // 异常处理：无法获取本机 IP 时保留原值
            }
        }

        // 4. 处理多级代理下的第一个有效 IP (X-Forwarded-For: client, proxy1, proxy2)
        if (ip != null && ip.length() > 15) {
            if (ip.contains(SEPARATOR)) {
                ip = ip.substring(0, ip.indexOf(SEPARATOR));
            }
        }

        return ip;
    }

    /**
     * 校验 IP 是否无效
     */
    private static boolean isIpInvalid(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }
}