package com.shop_service.common.core;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shop_service.model.response.RespPage;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分页转换器
 *
 * @author 啊祖
 * @date 2025-11-17 17:50
 **/
public class RespPageConvert {
    /**
     * 将 MyBatis-Plus 分页结果 IPage<E> 转换为 PageVo<V>
     *
     * @param page   原始分页对象（Entity）
     * @param voClass VO 类型
     * @param <E>    原始实体类型
     * @param <V>    目标 VO 类型
     * @return R<RespPage<V>> 封装后的分页结果
     */
    public static <E, V> RespPage<V> convert(IPage<E> page, Class<V> voClass) {
        List<V> voList = page.getRecords().stream().map(entity -> {
            try {
                // JDK 8+
                V vo = voClass.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(entity, vo);
                return vo;
            } catch (Exception e) {
                throw new RuntimeException("VO实例创建失败: " + voClass.getName(), e);
            }
        }).collect(Collectors.toList());

        RespPage<V> resp = new RespPage<>();
        resp.setRecords(voList);
        // 排除records
        BeanUtils.copyProperties(page, resp, "records");
        return resp;
    }
}
