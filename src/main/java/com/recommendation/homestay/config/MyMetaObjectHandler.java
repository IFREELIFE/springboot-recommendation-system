package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器
 *
 * 在插入和更新时自动填充实体的 createdAt 与 updatedAt 字段，保证所有实体时间戳一致且无需手动维护。
 *
 * 适用于标记了 @TableField(fill = FieldFill.INSERT) 或
 * @TableField(fill = FieldFill.INSERT_UPDATE) 的字段。
 *
 * @author Homestay Recommendation System
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入操作时自动填充
     * 将 createdAt 与 updatedAt 设为当前时间
     *
     * @param metaObject 实体元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新操作时自动填充
     * 仅更新 updatedAt 字段
     *
     * @param metaObject 实体元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
