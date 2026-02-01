package com.shop_service.common.core;

import com.shop_service.common.utils.ShopWebhookSignUtil;
import com.shop_service.model.entity.ShopWebhookEvent;
import com.shop_service.model.pojo.ShopInfo;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;

/**
 * 商户回调发送器
 * <p>
 * 回调鉴权方案:
 * - 每个商户配置一份 callbackSecret(Webhook Secret)
 * - 我方回调时对 payload 做 HMAC-SHA256 签名, 并将签名放入请求头
 * - 商户使用同一 callbackSecret 对收到的 payload 重新计算签名, 一致则确认回调来源为我方
 * <p>
 * 签名规则:
 * - data = timestamp + "\n" + rawBody
 * - signature = Base64(HMAC_SHA256(callbackSecret, data))
 * <p>
 * 商户验签要点:
 * - 取到 X-Timestamp, X-Signature, rawBody
 * - 按上述规则重算签名并比对
 * - 校验时间戳在允许窗口内(例如 5 分钟)以防重放
 * <p>
 * 请求头说明:
 * - X-Shop-No: 商户号, 用于商户识别来源账号(可选, 也可从 URL 路由或 payload 获取)
 * - X-Timestamp: 秒级时间戳, 用于签名与防重放
 * - X-Signature: 签名(Base64), 用于验证回调确实来自我方
 * - X-Event-Id: 事件ID, 建议商户用于幂等处理
 * - X-Event-Type: 事件类型, 方便商户路由处理
 *
 * @author 啊祖
 * @date 2026-01-14 19:01
 **/
@Slf4j
public class ShopWebhookSender {
    // HttpClient 复用(线程安全) connectTimeout 不要过大, 真实超时由 HttpRequest.timeout 控制
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    // 商户号请求头
    public static final String HEADER_SHOP_NO = "X-Shop-No";

    // 时间戳请求头(秒)
    public static final String HEADER_TIMESTAMP = "X-Timestamp";

    // 回调签名请求头(Base64)
    public static final String HEADER_SIGNATURE = "X-Signature";

    // 事件ID请求头(用于幂等)
    public static final String HEADER_EVENT_ID = "X-Event-Id";

    // 事件类型请求头
    public static final String HEADER_EVENT_TYPE = "X-Event-Type";

    // Content-Type
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    // JSON Content-Type
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * 发送商户回调
     *
     * @param shop  商户信息(至少包含: no, callbackSecret, callbackTimeoutMs)
     * @param event 回调事件(至少包含: callbackUrl, eventId, eventType, payload)
     * @return SendResult 发送结果(是否成功, httpCode, 响应内容/错误信息)
     */
    public static SendResult execute(ShopInfo shop, ShopWebhookEvent event) {
        String url = event.getWebhookUrl();
        String rawBody = event.getPayload() == null ? "" : event.getPayload();

        // 秒级足够, 且利于商户做时间窗口校验
        String timestamp = String.valueOf(Instant.now().getEpochSecond());

        // 回调签名: Base64(HMAC_SHA256(secret, timestamp + "\n" + rawBody))
        String secret = shop.getWebhookSecret();
        String signature = ShopWebhookSignUtil.signBase64(secret, timestamp, rawBody);

        // 默认 5s
        int timeoutMs = shop.getWebhookTimeoutMs() == null ? 5000 : shop.getWebhookTimeoutMs();

        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMillis(timeoutMs))
                    .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                    .header(HEADER_SHOP_NO, shop.getNo())
                    .header(HEADER_TIMESTAMP, timestamp)
                    .header(HEADER_SIGNATURE, signature)
                    .header(HEADER_EVENT_ID, event.getEventId())
                    .header(HEADER_EVENT_TYPE, event.getEventType())
                    .POST(HttpRequest.BodyPublishers.ofString(rawBody))
                    .build();

            HttpResponse<String> resp = HTTP_CLIENT.send(req, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();

            // 2xx 视为成功
            if (code >= 200 && code < 300) {
                return SendResult.success(code, safe(resp.body()));
            }

            return SendResult.fail(code, safe(resp.body()));
        } catch (Exception ex) {
            // -1 表示请求未到达或未拿到 HTTP 响应(超时/解析失败/网络异常等)
            return SendResult.fail(-1, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * 截断响应体/错误信息, 避免日志和数据库字段过长
     */
    private static String safe(String str) {
        if (str == null) return null;
        return str.length() > 500 ? str.substring(0, 500) : str;
    }

    /**
     * 发送结果
     *
     * @param ok       是否成功(2xx)
     * @param httpCode HTTP 状态码; -1 表示未获得 HTTP 响应
     * @param message  响应体(截断)或错误信息(截断)
     */
    public record SendResult(boolean ok, int httpCode, String message) {
        public static SendResult success(int code, String message) {
            return new SendResult(true, code, message);
        }

        public static SendResult fail(int code, String message) {
            return new SendResult(false, code, message);
        }
    }
}
