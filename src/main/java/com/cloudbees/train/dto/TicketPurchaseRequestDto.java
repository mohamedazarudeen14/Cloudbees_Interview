package com.cloudbees.train.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TicketPurchaseRequestDto {
    private String boardingStation;
    private String destinationStation;
    private double ticketPrice;
    private PassengerDto passengerDto;
}
