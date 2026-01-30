package com.shop_service.common.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * http响应
 *
 * @author 啊祖
 * @date 2026-01-21 19:26
 **/
@Slf4j
public class HttpResp {
    /**
     * 响应输出
     * @param response 响应
     * @param code     状态码
     * @param obj      对象
     * @throws IOException 异常
     */
    public static void writer(HttpServletResponse response, int code, Object obj) throws IOException {
        response.setStatus(code);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(JSON.toJSONString(obj, JSONWriter.Feature.WriteNulls));
    }
}
