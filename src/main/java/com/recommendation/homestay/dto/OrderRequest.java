package com.recommendation.homestay.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    
    @NotNull(message = "房源ID为必填项")
    private Long propertyId;
    
    @NotNull(message = "入住日期为必填项")
    private LocalDate checkInDate;
    
    @NotNull(message = "退房日期为必填项")
    private LocalDate checkOutDate;
    
    @NotNull(message = "入住人数为必填项")
    @Min(value = 1, message = "至少需要1位入住人")
    private Integer guestCount;
    
    private String remarks;
}
