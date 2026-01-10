package com.recommendation.homestay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PropertyOccupancyDTO {
    private Long id;
    private String title;
    private String city;
    private String district;
    private String address;
    private BigDecimal price;
    private Integer bedrooms;
    private Integer maxGuests;
    private String propertyType;
    private Integer bookingCount;
    private Integer occupiedRooms;
    private Integer remainingRooms;
    private Integer activeGuests;
    private Boolean available;
    private Long landlordId;
}
