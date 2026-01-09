package com.recommendation.homestay.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_property_interactions")
public class UserPropertyInteraction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    @TableField(exist = false)
    private User user;

    private Long propertyId;

    @TableField(exist = false)
    private Property property;

    private InteractionType type;

    private Integer rating; // 评分值，范围 1-5

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum InteractionType {
        VIEW, FAVORITE, BOOK, REVIEW
    }
}
