package com.cloudbees.train.server.mapper;

import com.cloudbees.train.SectionBooking;
import com.cloudbees.train.SectionBookingResponse;
import com.cloudbees.train.TicketPurchaseRequest;
import com.cloudbees.train.TicketReceiptResponse;
import com.cloudbees.train.server.entity.Seat;

import java.util.List;

import static com.cloudbees.train.server.constants.ApplicationConstants.TICKET_COST;

public class TicketMapper {
    public TicketReceiptResponse mapTicketReceiptForPurchase(Seat seat, String bookingId,
                                                             TicketPurchaseRequest ticketPurchaseRequest) {
        return TicketReceiptResponse.newBuilder()
                .setBoardingStation(ticketPurchaseRequest.getBoardingStation())
                .setDestinationStation(ticketPurchaseRequest.getDestinationStation())
                .setPassenger(ticketPurchaseRequest.getPassenger())
                .setPricePaid(TICKET_COST)
                .setSection(seat.getSectionName())
                .setSeatNumber(seat.getSeatNumber())
                .setBookingId(bookingId)
                .build();
    }

    public TicketReceiptResponse createNewTicketReceiptResponseFromExistOne(TicketReceiptResponse response,
                                                                            int seatNumber, String sectionName) {
        return TicketReceiptResponse.newBuilder()
                .setBookingId(response.getBookingId())
                .setSeatNumber(seatNumber)
                .setSection(sectionName)
                .setPricePaid(response.getPricePaid())
                .setBoardingStation(response.getBoardingStation())
                .setDestinationStation(response.getDestinationStation())
                .setPassenger(response.getPassenger())
                .build();
    }

    public SectionBookingResponse mapTicketReceiptToBookingsDto(List<TicketReceiptResponse> receiptDtos) {
        var sectionBookings = receiptDtos.stream().map(this::getBookingsDtoFromReceipt).toList();
        var sectionBookingResponse = SectionBookingResponse.newBuilder();
        sectionBookingResponse.addAllSectionBooking(sectionBookings);

        return sectionBookingResponse.build();
    }

    private SectionBooking getBookingsDtoFromReceipt(TicketReceiptResponse ticketReceiptDto) {
        return SectionBooking.newBuilder()
                .setPassenger(ticketReceiptDto.getPassenger())
                .setSectionName(ticketReceiptDto.getSection())
                .setSeatNumber(ticketReceiptDto.getSeatNumber())
                .build();
    }
}
