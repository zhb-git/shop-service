package com.shop_service.model.response;

import lombok.Data;

import java.util.List;

/**
 * @className: RespPage
 * @author: Java之父
 * @date: 2025/8/8 19:54
 * @version: 1.0.0
 * @description: 分页结果
 */
@Data
public class RespPage<T> {
    /**
     * 当前页码
     */
    private long pageNum;

    /**
     * 总页数
     */
    private long pageSize;

    /**
     * 每页条数
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 数据
     */
    private List<T> records;
}
