package com.recommendation.homestay.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("properties")
public class Property {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String city;

    private String district;

    private String address;

    private BigDecimal price;

    private Integer bedrooms;

    private Integer bathrooms;

    private Integer maxGuests;

    private String propertyType; // apartment, house, villa, etc.

    private String amenities; // JSON string of amenities

    private String images; // JSON string of image URLs

    private Boolean available = true;

    private Long landlordId;

    @TableField(exist = false)
    private User landlord;

    private BigDecimal rating = BigDecimal.ZERO;

    private Integer reviewCount = 0;

    private Integer viewCount = 0;

    private Integer bookingCount = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
