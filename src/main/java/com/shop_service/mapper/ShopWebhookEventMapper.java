package com.shop_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shop_service.model.entity.ShopWebhookEvent;
import com.shop_service.model.request.AdminShopWebhookEventPageQuery;
import com.shop_service.model.response.AdminShopWebhookEventVo;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商户回调事件Mapper
 *
 * @author 啊祖
 * @date 2026-01-14 16:37
 **/
public interface ShopWebhookEventMapper extends BaseMapper<ShopWebhookEvent> {
    /**
     * 查询到期需要投递的事件ID列表
     * 条件:
     * - deleted = 0
     * - status in (PENDING, FAILED_RETRY)
     * - next_retry_time is null OR next_retry_time <= now
     * - order by next_retry_time asc (null优先)
     * - limit
     */
    List<Long> selectDueEventIdList(@Param("now") LocalDateTime now,
                                    @Param("limit") int limit,
                                    @Param("pendingStatus") Integer pendingStatus,
                                    @Param("failedRetryStatus") Integer failedRetryStatus);

    /**
     * 分页查询
     * @param page  分页
     * @param query 参数
     * @return 结果
     */
    IPage<AdminShopWebhookEventVo> selectAdminShopWebhookEventVoPage(Page<?> page, @Param("query") AdminShopWebhookEventPageQuery query);

    /**
     * 查询商户所有失败的ID
     * @param shopId 商户ID
     * @return 结果
     */
    List<Long> selectShopAllFailIdList(@Param("shopId") Long shopId);
}
