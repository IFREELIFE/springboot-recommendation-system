package com.recommendation.homestay.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyAvailabilityDTO {
    private LocalDate date;
    private Integer bookedRooms;
    private Integer remainingRooms;
    private Integer bookedGuests;
    private Integer remainingGuests;
}
