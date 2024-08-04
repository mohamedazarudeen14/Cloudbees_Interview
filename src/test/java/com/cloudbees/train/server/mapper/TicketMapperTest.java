package com.cloudbees.train.server.mapper;

import com.cloudbees.train.SectionBooking;
import com.cloudbees.train.TicketPurchaseRequest;
import com.cloudbees.train.TicketReceiptResponse;
import com.cloudbees.train.server.entity.Seat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.cloudbees.train.server.constants.ApplicationConstants.TICKET_COST;
import static com.cloudbees.train.server.factory.TicketFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TicketMapperTest {
    @InjectMocks
    private TicketMapper ticketMapper;

    @Test
    void should_map_ticket_receipt_for_purchase() {
        var bookingId = "20240802";
        var seatMock = getSeatMock(1, 1);
        var purchaseRequest = getTicketPurchaseRequestDtoMock("Albert",
                "Einstein", "albert@gmail.com");
        var ticketReceipt = ticketMapper
                .mapTicketReceiptForPurchase(seatMock, bookingId, purchaseRequest);

        assertThat(ticketReceipt).isNotNull();
        assertTicketReceiptProperties(ticketReceipt, seatMock, bookingId, purchaseRequest);
    }

    @Test
    void should_map_ticket_receipts_to_section_bookings_dto() {
        var ticketReceipts = Collections.singletonList(getTicketReceiptDtoMock("Albert",
                "Einstein", "albert@gmail.com", 1, SECTION_A));

        var sectionBooking = ticketMapper.mapTicketReceiptToBookingsDto(ticketReceipts);

        assertThat(sectionBooking).isNotNull();
        assertThat(sectionBooking.getSectionBookingList()).hasSize(1);
        assertSectionBookingProperties(sectionBooking.getSectionBookingList().get(0), ticketReceipts.get(0));
    }

    @Test
    void should_create_new_ticket_receipt_from_existing_one() {
        var receipt = getTicketReceiptDtoMock("Albert", "Einstein",
                "albert@gmail.com", 1, SECTION_A);

        var newReceipt = ticketMapper
                .createNewTicketReceiptResponseFromExistOne(receipt, 2, SECTION_A);

        assertThat(newReceipt).isNotNull();
        assertEquals(newReceipt.getPassenger(), receipt.getPassenger());
        assertEquals(newReceipt.getBoardingStation(), receipt.getBoardingStation());
        assertEquals(newReceipt.getDestinationStation(), receipt.getDestinationStation());
        assertEquals(newReceipt.getBookingId(), receipt.getBookingId());
        assertEquals(newReceipt.getPricePaid(), receipt.getPricePaid());
        assertEquals(newReceipt.getSeatNumber(), 2);
        assertEquals(newReceipt.getSection(), SECTION_A);
    }

    private void assertSectionBookingProperties(SectionBooking sectionBookingsDto,
                                                TicketReceiptResponse ticketReceiptDto) {
        assertEquals(sectionBookingsDto.getPassenger(), ticketReceiptDto.getPassenger());
        assertEquals(sectionBookingsDto.getSectionName(), ticketReceiptDto.getSection());
        assertEquals(sectionBookingsDto.getSeatNumber(), ticketReceiptDto.getSeatNumber());
    }

    private void assertTicketReceiptProperties(TicketReceiptResponse ticketReceipt, Seat seat,
                                               String bookingId, TicketPurchaseRequest purchaseRequest) {
        assertEquals(ticketReceipt.getBoardingStation(), purchaseRequest.getBoardingStation());
        assertEquals(ticketReceipt.getDestinationStation(), purchaseRequest.getDestinationStation());
        assertEquals(ticketReceipt.getPassenger(), purchaseRequest.getPassenger());
        assertEquals(ticketReceipt.getSeatNumber(), seat.getSeatNumber());
        assertEquals(ticketReceipt.getSection(), SECTION_A);
        assertEquals(ticketReceipt.getPricePaid(), TICKET_COST);
        assertEquals(ticketReceipt.getBookingId(), bookingId);
    }
}