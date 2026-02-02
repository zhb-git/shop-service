package com.shop_service.biz;

import com.shop_service.common.constant.VsCardFundDetailType;
import com.shop_service.common.constant.VsCardStatus;
import com.shop_service.model.response.AdminStatsVo;
import com.shop_service.service.IShopCardFundDetailService;
import com.shop_service.service.IShopCardService;
import com.shop_service.service.IShopRechargeRecordService;
import com.shop_service.service.IShopService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 统计业务类
 *
 * @author 啊祖
 * @date 2026-02-02 15:43
 **/
@Slf4j
@Service
public class StatsServiceBiz {
    @Resource
    private IShopService shopService;

    @Resource
    private IShopRechargeRecordService shopRechargeRecordService;

    @Resource
    private IShopCardService shopCardService;

    @Resource
    private IShopCardFundDetailService shopCardFundDetailService;

    /**
     * 获取统计信息.
     *
     * <p>统计口径:
     * <ul>
     *   <li>date != null: 统计 date 当天的数据, 时间范围为 [date 00:00:00, date+1 00:00:00).</li>
     *   <li>date == null: 按各 service 默认逻辑统计(通常为全量).</li>
     * </ul>
     *
     * @param date 统计日期, 为空表示不限定日期(由下游 service 决定口径)
     * @return 统计结果
     */
    public AdminStatsVo getAdminStats(LocalDate date) {
        AdminStatsVo vo = new AdminStatsVo();

        // 商户数量
        vo.setShopSize(shopService.getShopSize(date));

        // 商户充值金额
        vo.setShopRechargeAmount(nvl(shopRechargeRecordService.getTotalAmount(date)));

        // 商户充值手续费
        vo.setShopRechargeFeeAmount(nvl(shopRechargeRecordService.getTotalFeeAmount(date)));

        // 卡片数量(全部, 正常, 冻结, 注销)
        vo.setCardSize(shopCardService.getCardSize(null, date));
        vo.setNormalCardSize(shopCardService.getCardSize(VsCardStatus.NORMAL, date));
        vo.setFrozenCardSize(shopCardService.getCardSize(VsCardStatus.FROZEN, date));
        vo.setDestroyCardSize(shopCardService.getCardSize(VsCardStatus.CANCELLED, date));

        // 卡片资金明细, 按账单类型统计金额
        vo.setCardConsumptionAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.CONSUMPTION, date)));
        vo.setCardTransferInAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.TRANSFER_IN, date)));
        vo.setCardTransferOutAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.TRANSFER_OUT, date)));
        vo.setCardFrozenAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.FROZEN, date)));
        vo.setCardUnfrozenAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.UN_FROZEN, date)));
        vo.setCardReversalAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.REVERSAL, date)));
        vo.setCardCreditAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.CREDIT, date)));
        vo.setCardFeeConsumptionAmount(nvl(shopCardFundDetailService.getTotalAmount(VsCardFundDetailType.FEE_CONSUMPTION, date)));

        return vo;
    }

    /**
     * BigDecimal 空值兜底, 统一返回 BigDecimal.ZERO.
     *
     * @param v 原值
     * @return v != null ? v : BigDecimal.ZERO
     */
    private BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
