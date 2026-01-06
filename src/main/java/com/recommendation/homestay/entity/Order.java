package com.recommendation.homestay.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNumber;

    private Long userId;

    @TableField(exist = false)
    private User user;

    private Long propertyId;

    @TableField(exist = false)
    private Property property;

    private LocalDate checkInDate;

    private LocalDate checkOutDate;

    private Integer guestCount;

    private BigDecimal totalPrice;

    private OrderStatus status = OrderStatus.PENDING;

    private String remarks;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum OrderStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED
    }
}
