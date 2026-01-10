package com.recommendation.homestay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PropertyResponseDTO {
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
    private String propertyType;
    private String amenities;
    private Boolean available;
    private Long landlordId;
    private BigDecimal rating;
    private Integer reviewCount;
    private Integer viewCount;
    private Integer bookingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String images;
    private List<String> imagesBase64;
    private Integer remainingRooms;
    private List<DailyAvailabilityDTO> upcomingAvailability;
}
