package com.cloudbees.train.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class TicketReceiptDto {
    private PassengerDto passengerDto;
    private String boardingStation;
    private String destinationStation;
    private double pricePaid;
    private String section;
    private int seatNumber;
    private String bookingId;
}
