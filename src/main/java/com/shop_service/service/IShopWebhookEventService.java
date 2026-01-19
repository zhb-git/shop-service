package com.shop_service.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shop_service.common.constant.ShopWebhookEventType;
import com.shop_service.model.entity.ShopWebhookEvent;
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
     * 发布一个商户回调事件(落库)
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
    List<Long> getDueEventList(LocalDateTime now, int limit);

    /**
     * 领取一个回调事件并标记为发送中(SENDING)
     * 说明:
     * - 这是一个"条件更新"(CAS)操作, 用于避免同一条事件被重复发送
     * - 只有当事件当前状态为 PENDING/FAILED_RETRY 且已到期时, 才会更新为 SENDING
     * - 领取成功返回 true, 否则返回 false(表示已被其他线程/任务领取或不满足条件)
     *
     * @param id  事件表主键
     * @param now 当前时间
     * @return 是否领取成功
     */
    boolean markSending(Long id, LocalDateTime now);

    /**
     * 标记回调事件发送成功(SUCCESS)
     * 说明:
     * - status -> SUCCESS
     * - nextRetryTime -> null
     * - lastError -> null
     *
     * @param id 事件表主键
     */
    void markSuccess(Long id);

    /**
     * 标记回调事件发送失败(待重试)(FAILED_RETRY)
     * 说明:
     * - status -> FAILED_RETRY
     * - retryCount -> 递增后的重试次数
     * - nextRetryTime -> 下次重试时间(由调度策略计算)
     * - lastError -> 记录失败原因(建议截断避免过长)
     *
     * @param id            事件表主键
     * @param retryCount    已重试次数(递增后)
     * @param nextRetryTime 下次重试时间
     * @param lastError     失败原因(简短)
     */
    void markFailRetry(Long id, int retryCount, LocalDateTime nextRetryTime, String lastError);

    /**
     * 标记回调事件终止/放弃(ABANDONED)
     * 适用场景:
     * - 商户停用
     * - 商户关闭回调
     * - 缺少 callbackUrl / callbackSecret 等关键配置
     * - 超过最大重试次数(可在此处终止)
     *
     * @param id        事件表主键
     * @param lastError 放弃原因(简短)
     */
    void markAbandoned(Long id, String lastError);

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

    /**
     * 查询回调事件
     * @param id 系统ID
     * @return 事件
     */
    ShopWebhookEvent getById(Long id);
}
