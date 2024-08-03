package com.cloudbees.train.mapper;

import com.cloudbees.train.factory.TicketFactory;
import com.cloudbees.train.dto.SectionBookingsDto;
import com.cloudbees.train.dto.TicketPurchaseRequestDto;
import com.cloudbees.train.dto.TicketReceiptDto;
import com.cloudbees.train.entity.Seat;
import com.cloudbees.train.enums.TrainSection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class TicketMapperTest {
    @InjectMocks
    private TicketMapper ticketMapper;

    @Test
    void should_map_ticket_receipt_for_purchase() throws Exception {
        var bookingId = "20240802";
        var seatMock = TicketFactory.getSeatMock(1, TrainSection.SECTION_A.getSectionId());
        var purchaseRequest = TicketFactory.getTicketPurchaseRequestDtoMock("Albert",
                "Einstein", "albert@gmail.com");
        var ticketReceipt = ticketMapper
                .mapTicketReceiptForPurchase(seatMock, bookingId, purchaseRequest);

        assertThat(ticketReceipt).isNotNull();
        assertTicketReceiptProperties(ticketReceipt, seatMock, bookingId, purchaseRequest);
    }

    @Test
    void should_map_ticket_receipts_to_section_bookings_dto() {
        var ticketReceipts = Collections.singletonList(TicketFactory.getTicketReceiptDtoMock("Albert",
                "Einstein", "albert@gmail.com", 1, TrainSection.SECTION_A.getSectionName()));

        var sectionBookings = ticketMapper.mapTicketReceiptToBookingsDto(ticketReceipts);

        assertThat(sectionBookings).isNotNull().hasSize(ticketReceipts.size());
        assertSectionBookingProperties(sectionBookings.get(0), ticketReceipts.get(0));
    }

    private void assertSectionBookingProperties(SectionBookingsDto sectionBookingsDto,
                                                TicketReceiptDto ticketReceiptDto) {
        assertEquals(sectionBookingsDto.getPassengerDto(), ticketReceiptDto.getPassengerDto());
        assertEquals(sectionBookingsDto.getSectionName(), ticketReceiptDto.getSection());
        assertEquals(sectionBookingsDto.getSeatNumber(), ticketReceiptDto.getSeatNumber());
    }

    private void assertTicketReceiptProperties(TicketReceiptDto ticketReceipt, Seat seat,
                                               String bookingId, TicketPurchaseRequestDto purchaseRequest) throws Exception {
        assertEquals(ticketReceipt.getBoardingStation(), purchaseRequest.getBoardingStation());
        assertEquals(ticketReceipt.getDestinationStation(), purchaseRequest.getDestinationStation());
        assertEquals(ticketReceipt.getPassengerDto(), purchaseRequest.getPassengerDto());
        assertEquals(ticketReceipt.getSeatNumber(), seat.getSeatNumber());
        assertEquals(ticketReceipt.getSection(), TrainSection.from(seat.getSectionId()).getSectionName());
        assertEquals(ticketReceipt.getPricePaid(), purchaseRequest.getTicketPrice());
        assertEquals(ticketReceipt.getBookingId(), bookingId);
    }
}