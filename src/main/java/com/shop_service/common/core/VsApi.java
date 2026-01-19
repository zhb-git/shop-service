package com.shop_service.common.core;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.shop_service.common.constant.VsCardTransferType;
import com.shop_service.config.VsConfig;
import com.shop_service.exception.ApiRequestException;
import com.shop_service.model.pojo.VsApiResult;
import com.shop_service.model.pojo.VsCardBalance;
import com.shop_service.model.pojo.VsCardBin;
import com.shop_service.model.pojo.VsCardInfo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * vs接口
 *
 * @author 啊祖
 * @date 2026-01-14 22:52
 **/
@Slf4j
@Component
public class VsApi {
    @Resource
    private VsConfig vsConfig;

    /**
     * 获取卡头列表
     *
     * @return 卡头列表
     */
    public List<VsCardBin> getCardBinList() {
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/cardBin/getList")
                .header("X-Token", vsConfig.getToken());
        return execute(request, new TypeReference<>() {
        });
    }

    /**
     * 发起开卡
     *
     * @param cardBinId     卡头ID
     * @param reserveAmount 预留金额
     * @return 交易ID
     */
    public String openCard(Long cardBinId, BigDecimal reserveAmount) {
        Map<String, Object> params = new HashMap<>();
        params.put("binId", cardBinId);
        params.put("reserveAmount", reserveAmount);
        String json = JSON.toJSONString(params);
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/card/open")
                .header("X-Token", vsConfig.getToken())
                .body(json);
        return execute(request, new TypeReference<>() {
        });
    }

    /**
     * 获取卡片余额
     * @param cardId 卡片ID
     * @return 余额
     */
    public VsCardBalance getCardBalance(String cardId) {
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/card/getBalance?cardId=" + cardId)
                .header("X-Token", vsConfig.getToken());
        return execute(request, new TypeReference<>() {
        });
    }

    /**
     * 注销卡片
     * @param cardId 卡片ID
     * @return 交易ID
     */
    public String destroyCard(String cardId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cardId", cardId);
        String json = JSON.toJSONString(params);
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/card/destroy")
                .header("X-Token", vsConfig.getToken())
                .body(json);
        return execute(request, new TypeReference<>() {
        });
    }

    /**
     * 冻结卡片
     * @param cardId 卡片ID
     * @return 交易ID
     */
    public String freezeCard(String cardId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cardId", cardId);
        String json = JSON.toJSONString(params);
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/card/freeze")
                .header("X-Token", vsConfig.getToken())
                .body(json);
        return execute(request, new TypeReference<>() {
        });
    }

    /**
     * 解冻结卡片
     * @param cardId 卡片ID
     * @return 交易ID
     */
    public String unfreezeCard(String cardId) {
        Map<String, Object> params = new HashMap<>();
        params.put("cardId", cardId);
        String json = JSON.toJSONString(params);
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/card/unfreeze")
                .header("X-Token", vsConfig.getToken())
                .body(json);
        return execute(request, new TypeReference<>() {
        });
    }

    /**
     * 卡片转账
     * @param cardId       卡片ID
     * @param transferType 转账类型
     * @param amount       转账金额
     * @return 交易ID
     */
    public String cardTransfer(String cardId, VsCardTransferType transferType, BigDecimal amount) {
        Map<String, Object> params = new HashMap<>();
        params.put("cardId", cardId);
        params.put("type", transferType.getValue());
        params.put("amount", amount);
        String json = JSON.toJSONString(params);
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/card/transfer")
                .header("X-Token", vsConfig.getToken())
                .body(json);
        return execute(request, new TypeReference<>() {
        });
    }

    /**
     * 查询卡片信息
     * @param cardId 卡片ID
     * @return 卡片信息
     */
    public VsCardInfo getCardInfo(String cardId) {
        HttpRequest request = HttpRequest.get(vsConfig.getApi() + "/clientServe/card/getCardInfo?cardId=" + cardId)
                .header("X-Token", vsConfig.getToken());
        return execute(request, new TypeReference<>() {
        });
    }

    private <T> T execute(HttpRequest request, TypeReference<VsApiResult<T>> type) {
        try (HttpResponse response = request.execute()) {
            VsApiResult<T> result = JSON.parseObject(response.body(), type);
            boolean success = result.isSuccess();
            if (!success) throw new ApiRequestException("业务失败: " + result.getMessage());
            return result.getData();
        } catch (Exception e) {
            log.error("vs卡片接口({})调用失败", request.getUrl(), e);
            throw new ApiRequestException("API通信异常", e);
        }
    }
}
