package com.shop_service.web.controller.admin;

import com.shop_service.biz.StatsServiceBiz;
import com.shop_service.model.response.AdminStatsVo;
import com.shop_service.model.response.R;
import jakarta.annotation.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 统计接口
 *
 * @author 啊祖
 * @date 2026-02-02 15:32
 **/
@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {
    @Resource
    private StatsServiceBiz statsServiceBiz;

    /**
     * 获取统计信息
     *
     * @param date 时间 (yyyy-MM-dd)
     * @return 响应
     */
    @GetMapping("/get")
    R<AdminStatsVo> get(
            @RequestParam(name = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AdminStatsVo stats = statsServiceBiz.getAdminStats(date);
        return R.success(stats);
    }
}
