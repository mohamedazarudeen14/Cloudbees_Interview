package com.cloudbees.train.mapper;

import com.cloudbees.train.dto.SectionBookingsDto;
import com.cloudbees.train.entity.Seat;
import com.cloudbees.train.dto.TicketPurchaseRequestDto;
import com.cloudbees.train.dto.TicketReceiptDto;
import com.cloudbees.train.enums.TrainSection;

import java.util.List;

public class TicketMapper {
    public TicketReceiptDto mapTicketReceiptForPurchase(Seat seat, String bookingId,
                                                        TicketPurchaseRequestDto ticketPurchaseRequestDto) throws Exception {
        return TicketReceiptDto.builder()
                .boardingStation(ticketPurchaseRequestDto.getBoardingStation())
                .destinationStation(ticketPurchaseRequestDto.getDestinationStation())
                .passengerDto(ticketPurchaseRequestDto.getPassengerDto())
                .pricePaid(ticketPurchaseRequestDto.getTicketPrice())
                .section(TrainSection.from(seat.getSectionId()).getSectionName())
                .seatNumber(seat.getSeatNumber())
                .bookingId(bookingId)
                .build();
    }

    public List<SectionBookingsDto> mapTicketReceiptToBookingsDto(List<TicketReceiptDto> receiptDtos) {
        return receiptDtos.stream().map(this::getBookingsDtoFromReceipt).toList();
    }

    private SectionBookingsDto getBookingsDtoFromReceipt(TicketReceiptDto ticketReceiptDto) {
        return SectionBookingsDto.builder()
                .passengerDto(ticketReceiptDto.getPassengerDto())
                .sectionName(ticketReceiptDto.getSection())
                .seatNumber(ticketReceiptDto.getSeatNumber())
                .build();
    }
}
