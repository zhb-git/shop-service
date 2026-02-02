package com.shop_service.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * mybatis-plus时间条件填充工具类
 *
 * @author 啊祖
 * @date 2026-02-02 15:57
 **/
@Slf4j
public class MpTimeRangeUtil {
    /**
     * 时间范围对象, 使用左闭右开区间 [start, endExclusive).
     *
     * <p>字段说明:
     * <ul>
     *   <li>start: 起始时间(包含).</li>
     *   <li>endExclusive: 结束时间(不包含).</li>
     * </ul>
     *
     * <p>示例: 2026-01-11 的范围:
     * <pre>
     *   start = 2026-01-11 00:00:00
     *   endExclusive = 2026-01-12 00:00:00
     * </pre>
     */
    public record TimeRange(LocalDateTime start, LocalDateTime endExclusive) {
        public TimeRange {
            Objects.requireNonNull(start, "start must not be null");
            Objects.requireNonNull(endExclusive, "endExclusive must not be null");
        }
    }

    /**
     * 根据指定时间点计算其所在日期的 [start, endExclusive) 范围.
     *
     * <p>返回值:
     * <ul>
     *   <li>dateTime 为 null 时返回 null.</li>
     *   <li>否则返回 dateTime 所在日期 00:00:00 到次日 00:00:00 的范围.</li>
     * </ul>
     *
     * @param dateTime 任意时间点, 仅取其日期部分
     * @return 当天的时间范围(TimeRange), 入参为 null 则返回 null
     */
    public static TimeRange dayRange(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dayRange(dateTime.toLocalDate());
    }

    /**
     * 根据指定日期计算 [start, endExclusive) 范围.
     *
     * <p>范围定义:
     * <pre>
     *   start = day.atStartOfDay()
     *   endExclusive = start.plusDays(1)
     * </pre>
     *
     * @param day 指定日期
     * @return 当天的时间范围(TimeRange), 入参为 null 则返回 null
     */
    public static TimeRange dayRange(LocalDate day) {
        if (day == null) {
            return null;
        }
        LocalDateTime start = day.atStartOfDay();
        LocalDateTime endExclusive = start.plusDays(1);
        return new TimeRange(start, endExclusive);
    }

    /**
     * 在 wrapper 上追加 "某一天" 的时间范围过滤条件, 针对 LocalDateTime 入参.
     *
     * <p>追加的条件为:
     * <pre>
     *   column &gt;= start AND column &lt; endExclusive
     * </pre>
     *
     * <p>注意:
     * <ul>
     *   <li>wrapper 或 column 或 dateTime 为 null 时, 不追加任何条件.</li>
     *   <li>本方法只追加条件, 不负责创建 wrapper.</li>
     * </ul>
     *
     * @param wrapper 目标 LambdaQueryWrapper
     * @param column  要过滤的时间字段(如 Shop::getCreateTime)
     * @param dateTime 指定时间点, 仅取其日期部分
     * @param <T>     实体类型
     */
    public static <T> void applyDayRange(LambdaQueryWrapper<T> wrapper,
                                         SFunction<T, LocalDateTime> column,
                                         LocalDateTime dateTime) {
        applyRange(wrapper, column, dayRange(dateTime));
    }

    /**
     * 在 wrapper 上追加 "某一天" 的时间范围过滤条件, 针对 LocalDate 入参.
     *
     * <p>追加的条件为:
     * <pre>
     *   column &gt;= start AND column &lt; endExclusive
     * </pre>
     *
     * <p>注意:
     * <ul>
     *   <li>wrapper 或 column 或 day 为 null 时, 不追加任何条件.</li>
     * </ul>
     *
     * @param wrapper 目标 LambdaQueryWrapper
     * @param column  要过滤的时间字段(如 Shop::getUpdateTime)
     * @param day     指定日期
     * @param <T>     实体类型
     */
    public static <T> void applyDayRange(LambdaQueryWrapper<T> wrapper,
                                         SFunction<T, LocalDateTime> column,
                                         LocalDate day) {
        applyRange(wrapper, column, dayRange(day));
    }

    /**
     * 将指定的时间范围以左闭右开区间的方式追加到 wrapper 上.
     *
     * <p>追加规则:
     * <pre>
     *   column &gt;= range.start AND column &lt; range.endExclusive
     * </pre>
     *
     * <p>防御性处理:
     * <ul>
     *   <li>任一入参为 null 时直接返回, 不抛异常, 不记录日志.</li>
     * </ul>
     *
     * @param wrapper 目标 LambdaQueryWrapper
     * @param column  时间字段
     * @param range   时间范围(左闭右开)
     * @param <T>     实体类型
     */
    private static <T> void applyRange(LambdaQueryWrapper<T> wrapper,
                                       SFunction<T, LocalDateTime> column,
                                       TimeRange range) {
        if (wrapper == null || column == null || range == null) {
            return;
        }
        wrapper.ge(column, range.start())
                .lt(column, range.endExclusive());
    }
}
