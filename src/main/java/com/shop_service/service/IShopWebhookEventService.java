package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.common.constant.ShopWebhookEventType;
import com.shop_service.model.entity.ShopWebhookEvent;
import com.shop_service.model.pojo.DueEvent;
import com.shop_service.model.pojo.ShopInfo;
import com.shop_service.model.request.AdminShopWebhookEventPageQuery;
import com.shop_service.model.response.AdminShopWebhookEventVo;
import com.shop_service.model.response.RespPage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户回调事件服务层
 *
 * @author 啊祖
 * @date 2026-01-14 16:37
 **/
public interface IShopWebhookEventService extends IService<ShopWebhookEvent> {

    /**
     * 发布一个商户回调事件(落库&&首发)
     * 说明:
     * - 会将事件写入 shop_webhook_event 表
     * - 若商户开启回调且配置完整, 状态置为 PENDING 并等待定时任务扫描发送
     * - 若商户未开启回调/商户停用/缺少回调配置, 状态置为 ABANDONED 并记录原因
     *
     * @param shopInfo  商户信息
     * @param eventType 回调事件类型
     * @param payload   回调数据(会序列化为 JSON)
     */
    void publish(ShopInfo shopInfo, ShopWebhookEventType eventType, Object payload);

    /**
     * 获取到期待发送的回调事件列表(分页/批量)
     * 条件:
     * - status in (PENDING, FAILED_RETRY)
     * - nextRetryTime <= now (或 nextRetryTime 为空)
     * - deleted = 0
     * 用途:
     * - 给定时任务扫描拉取, 批量分发发送
     *
     * @param now   当前时间
     * @param limit 本次最多返回条数
     * @return 到期事件列表
     */
    List<DueEvent> getDueEventList(LocalDateTime now, int limit);

    /**
     * 发送回调
     * @param shopInfo 商户信息
     * @param id       系统回调事件ID
     */
    void sendWebhook(ShopInfo shopInfo, Long id);

    /**
     * 分页查询
     * @param query 参数
     * @return 结果
     */
    RespPage<AdminShopWebhookEventVo> getAdminShopWebhookEventVoPage(AdminShopWebhookEventPageQuery query);

    /**
     * 重试回调
     * @param ids 系统事件ID
     */
    void retry(Long... ids);

    /**
     * 重试商户所有失败回调事件
     * @param shopId 商户ID
     */
    void retryAllFail(Long shopId);
}
