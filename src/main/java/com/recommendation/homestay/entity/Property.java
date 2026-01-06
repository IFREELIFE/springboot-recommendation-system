package com.recommendation.homestay.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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

    // 核心修改1：给createdAt添加序列化注解
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    // 核心修改2：给updatedAt添加序列化注解
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updatedAt;
}