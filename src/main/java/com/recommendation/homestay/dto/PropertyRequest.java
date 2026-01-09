package com.recommendation.homestay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequest {
    
    @NotBlank(message = "标题为必填项")
    private String title;
    
    private String description;
    
    @NotBlank(message = "城市为必填项")
    private String city;
    
    @NotBlank(message = "区域为必填项")
    private String district;
    
    @NotBlank(message = "地址为必填项")
    private String address;
    
    @NotNull(message = "价格为必填项")
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal price;
    
    @NotNull(message = "卧室数量为必填项")
    @Min(value = 1, message = "至少需要1间卧室")
    private Integer bedrooms;
    
    @NotNull(message = "卫生间数量为必填项")
    @Min(value = 1, message = "至少需要1间卫生间")
    private Integer bathrooms;
    
    @NotNull(message = "可入住人数为必填项")
    @Min(value = 1, message = "至少可入住1人")
    private Integer maxGuests;
    
    private String propertyType;
    
    private String amenities;
    
    private String images;
}
