package com.recommendation.homestay.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体
 *
 * 代表民宿推荐系统中的用户，使用 MyBatis-Plus 注解进行 ORM 映射。
 *
 * 用户角色：
 * - USER：普通用户，可浏览与预订房源
 * - LANDLORD：房东，可发布与管理房源
 * - ADMIN：管理员，拥有全部权限
 *
 * @author Homestay Recommendation System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    /**
     * 用户ID（主键），数据库自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 登录使用的唯一用户名
     */
    private String username;

    /**
     * 加密后的密码
     */
    private String password;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户手机号
     */
    private String phone;

    /**
     * 用户头像地址
     */
    private String avatar;

    /**
     * 用户角色（USER、LANDLORD、ADMIN）
     */
    private Role role = Role.USER;

    /**
     * 账户是否启用
     */
    private Boolean enabled = true;

    /**
     * 创建时间，插入时由 MyMetaObjectHandler 自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间，插入与更新时自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 用户角色枚举
     */
    public enum Role {
        USER, LANDLORD, ADMIN
    }
}
