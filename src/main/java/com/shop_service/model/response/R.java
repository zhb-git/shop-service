package com.shop_service.model.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop_service.common.core.RespPageConvert;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

/**
 * @className: R
 * @author: Java之父
 * @date: 2025/8/2 15:35
 * @version: 1.0.0
 * @description: 统一响应
 */
@Data
public class R<T> implements Serializable {
    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误提示信息
     */
    private String message;

    /**
     * 附加返回数据
     */
    private T data;

    public static <T> R<T> success() {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "操作成功";
        return r;
    }

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.code = 200;
        r.message = "操作成功";
        r.data = data;
        return r;
    }

    /**
     * 将 MyBatis-Plus 分页结果 IPage<E> 转换为 PageVO<V>
     *
     * @param page   原始分页对象（Entity）
     * @param voClass VO 类型
     * @param <E>    原始实体类型
     * @param <V>    目标 VO 类型
     * @return R<RespPage<V>> 封装后的分页结果
     */
    public static <E, V> R<RespPage<V>> success(IPage<E> page, Class<V> voClass) {
        RespPage<V> resp = RespPageConvert.convert(page, voClass);
        return success(resp);
    }

    public static <E> R<RespPage<E>> success(IPage<E> page) {
        RespPage<E> resp = new RespPage<>();
        resp.setRecords(page.getRecords());
        // 排除records
        BeanUtils.copyProperties(page, resp, "records");
        return success(resp);
    }

    public static R<String> fail() {
        R<String> r = new R<>();
        r.code = 999;
        r.message = "操作失败";
        return r;
    }

    public  static <T> R<T> fail(String message) {
        R<T> r = new R<>();
        r.code = 999;
        r.message = message;
        return r;
    }

    public static <T> R<T> error() {
        R<T> r = new R<>();
        r.code = 500;
        r.message = "系统异常";
        return r;
    }

    public static <T> R<T> error(String message) {
        R<T> r = new R<>();
        r.code = 500;
        r.message = message;
        return r;
    }
}
