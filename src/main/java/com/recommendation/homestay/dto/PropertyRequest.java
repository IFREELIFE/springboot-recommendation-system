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
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    @NotBlank(message = "City is required")
    private String city;
    
    @NotBlank(message = "District is required")
    private String district;
    
    @NotBlank(message = "Address is required")
    private String address;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Bedrooms is required")
    @Min(value = 1, message = "Must have at least 1 bedroom")
    private Integer bedrooms;
    
    @NotNull(message = "Bathrooms is required")
    @Min(value = 1, message = "Must have at least 1 bathroom")
    private Integer bathrooms;
    
    @NotNull(message = "Max guests is required")
    @Min(value = 1, message = "Must accommodate at least 1 guest")
    private Integer maxGuests;
    
    private String propertyType;
    
    private String amenities;
    
    private String images;
}
