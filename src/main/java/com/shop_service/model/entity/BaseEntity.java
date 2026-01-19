package com.shop_service.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @className: BaseEntity
 * @author: Java之父
 * @date: 2025/9/29 0:22
 * @version: 1.0.0
 * @description: 公共基础实体, 带主键 创建时间 更新时间
 */
@Getter
@Setter
public abstract class BaseEntity implements Serializable {
    /**
     * 主键 (雪花算法)
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除逻辑
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer deleted;
}
