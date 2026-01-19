package com.shop_service.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * @className: MybatisPlusConfig
 * @author: java之父
 * @date: 2025/6/18 16:18
 * @version: 1.0.0
 * @description: mybatis-plus配置类
 */
@Configuration
public class MybatisPlusConfig implements MetaObjectHandler {
    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 如果配置多个插件, 切记分页最后添加
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        // 如果有多数据源可以不配具体类型, 否则都建议配上具体的 DbType
        return interceptor;
    }

    /**
     * 添加时处理
     * - 存在默认值则不会覆盖
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime::now, LocalDateTime.class);
        // 默认未删除
        this.strictInsertFill(metaObject, "deleted", () -> 0, Integer.class);
    }

    /**
     * 更改时处理
     * 必须使用 updateById(entity) 或 update(entity, wrapper)
     * 也就是传入一个实体对象
     * 如果只用 update(wrapper) 不会生效
     * 且需自动更改的数据必须为空
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 强制每次更新
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
    }
}
