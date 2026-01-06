package com.recommendation.homestay.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus Automatic Field Fill Handler
 * 
 * Automatically fills createdAt and updatedAt timestamp fields for entities
 * during database insert and update operations. This ensures consistent
 * timestamp management across all entities without manual intervention.
 * 
 * Works with entities that have @TableField(fill = FieldFill.INSERT) or
 * @TableField(fill = FieldFill.INSERT_UPDATE) annotations.
 * 
 * @author Homestay Recommendation System
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * Automatic field fill during INSERT operations
     * Sets both createdAt and updatedAt to current timestamp
     * 
     * @param metaObject Entity meta object
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * Automatic field fill during UPDATE operations
     * Updates only the updatedAt timestamp
     * 
     * @param metaObject Entity meta object
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
