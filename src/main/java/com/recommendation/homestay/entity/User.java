package com.recommendation.homestay.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Entity
 * 
 * Represents a user in the homestay recommendation system.
 * Uses MyBatis-Plus annotations for ORM mapping.
 * 
 * User roles:
 * - USER: Regular user who can browse and book properties
 * - LANDLORD: Property owner who can list and manage properties
 * - ADMIN: System administrator with full access
 * 
 * @author Homestay Recommendation System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    /**
     * User ID (Primary Key)
     * Auto-incremented by database
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Unique username for login
     */
    private String username;

    /**
     * Encrypted password
     */
    private String password;

    /**
     * User email address
     */
    private String email;

    /**
     * User phone number
     */
    private String phone;

    /**
     * User avatar URL
     */
    private String avatar;

    /**
     * User role (USER, LANDLORD, ADMIN)
     */
    private Role role = Role.USER;

    /**
     * Account enabled status
     */
    private Boolean enabled = true;

    /**
     * Record creation timestamp
     * Automatically filled by MyMetaObjectHandler on insert
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * Record last update timestamp
     * Automatically filled by MyMetaObjectHandler on insert and update
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * User role enumeration
     */
    public enum Role {
        USER, LANDLORD, ADMIN
    }
}
