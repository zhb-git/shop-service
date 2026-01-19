package com.shop_service.common.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 商户回调签名工具类
 * <p>
 * 签名用途:
 * - 我方回调给商户时, 使用商户配置的 WebhookSecret 对回调内容生成签名
 * - 商户收到回调后, 使用同一 callbackSecret 按相同规则重算签名并比对, 用于确认回调来源为我方
 * <p>
 * 签名规则(最简可用):
 * - data = timestamp + "\n" + rawBody
 * - signature = Base64( HMAC-SHA256(secret, data) )
 * <p>
 * 说明:
 * - timestamp 建议使用秒级时间戳(Instant.now().getEpochSecond()), 商户可做时间窗口校验防重放(例如 5 分钟)
 * - rawBody 必须使用"原始请求体字符串"(不要二次序列化), 否则商户侧验签可能不一致
 *
 * @author 啊祖
 * @date 2026-01-14 18:57
 **/
public class ShopWebhookSignUtil {

    /**
     * 生成回调签名(Base64)
     *
     * @param secret    商户回调签名密钥(callbackSecret/Webhook Secret)
     * @param timestamp 秒级时间戳字符串, 用于参与签名与防重放
     * @param rawBody   原始请求体(JSON 字符串), 需与发送时完全一致
     * @return Base64 编码后的签名字符串
     */
    public static String signBase64(String secret, String timestamp, String rawBody) {
        String data = timestamp + "\n" + (rawBody == null ? "" : rawBody);
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            // 属于系统级错误: 算法不可用或密钥初始化失败等
            throw new IllegalStateException("Webhook sign failed", e);
        }
    }
}
