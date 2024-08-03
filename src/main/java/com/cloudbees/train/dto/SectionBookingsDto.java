package com.cloudbees.train.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SectionBookingsDto {
    private PassengerDto passengerDto;
    private int seatNumber;
    private String sectionName;
}
