package com.cloudbees.train.service;

import com.cloudbees.train.constants.ApplicationConstants;
import com.cloudbees.train.enums.TrainSection;
import com.cloudbees.train.mapper.TicketMapper;
import com.cloudbees.train.persistence.TrainSeatManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.cloudbees.train.factory.TicketFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TicketManagerServiceTest {
    @Mock
    private TrainSeatManager trainSeatManager;
    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketManagerService ticketManagerService;

    @Test
    void should_throw_exception_when_no_seats_are_available_for_booking() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsMapMockWithoutEmptySeats());

        assertThatCode(() -> ticketManagerService.getTrainTicketReceipt(purchaseDto))
                .isInstanceOf(Exception.class).hasMessage(ApplicationConstants.NO_SEATS_AVAILABLE_FOR_NEW_BOOKING);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_passenger_details_has_empty_values() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(null, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());

        assertThatCode(() -> ticketManagerService.getTrainTicketReceipt(purchaseDto))
                .isInstanceOf(Exception.class).hasMessage(ApplicationConstants.PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE);

        var purchaseDto_1 = getTicketPurchaseRequestDtoMock(FIRST_NAME,
                null, EMAIL_ADDRESS);

        assertThatCode(() -> ticketManagerService.getTrainTicketReceipt(purchaseDto_1))
                .isInstanceOf(Exception.class).hasMessage(ApplicationConstants.PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE);

        var purchaseDto_2 = getTicketPurchaseRequestDtoMock(FIRST_NAME,
                LAST_NAME, null);

        assertThatCode(() -> ticketManagerService.getTrainTicketReceipt(purchaseDto_2))
                .isInstanceOf(Exception.class).hasMessage(ApplicationConstants.PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE);

        verify(trainSeatManager, times(3)).getTrainSeats();
        verify(trainSeatManager, times(3)).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_provided_passenger_email_is_not_valid() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(FIRST_NAME,
                LAST_NAME, "azar.gmail.com");

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());

        assertThatCode(() -> ticketManagerService.getTrainTicketReceipt(purchaseDto))
                .isInstanceOf(Exception.class).hasMessage(ApplicationConstants.EMAIL_ADDRESS_FORMAT_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_passenger_exist_with_same_name_and_email_address() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        assertThatCode(() -> ticketManagerService.getTrainTicketReceipt(purchaseDto))
                .isInstanceOf(Exception.class).hasMessage(ApplicationConstants.PASSENGER_EXIST_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_execute_ticket_booking() throws Exception {
        var purchaseDto = getTicketPurchaseRequestDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS_2);
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var ticketReceipt = getTicketReceiptDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS,
                2, TrainSection.SECTION_A.getSectionName());

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(ticketMapper.mapTicketReceiptForPurchase(any(), any(), any())).willReturn(ticketReceipt);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        var response = ticketManagerService.getTrainTicketReceipt(purchaseDto);

        assertThat(response).isNotNull();
        assertThat(response.getSeatNumber()).isEqualTo(2);
        assertThat(response.getSection()).isEqualTo(TrainSection.SECTION_A.getSectionName());

        verify(trainSeatManager).getSeatBookings();
        verify(trainSeatManager).getTrainSeats();
        verify(ticketMapper).mapTicketReceiptForPurchase(any(), any(), any());
    }

    @Test
    void should_throw_exception_to_get_receipt_by_booking_id() {
        String bookingId = "2045678900";
        var seats = getTrainSeatsWithAvailableSeatsMock();

        given(trainSeatManager.getTrainSeats()).willReturn(seats);

        assertThatCode(() -> ticketManagerService.getBookedTicketReceipt(bookingId, EMAIL_ADDRESS))
                .isInstanceOf(Exception.class)
                .hasMessage(ApplicationConstants.NO_BOOKING_FOUND_ERROR_MESSAGE + bookingId);

        verify(trainSeatManager).getTrainSeats();
    }

    @Test
    void should_throw_error_when_email_address_not_valid_to_get_already_booked_receipt() {
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        assertThatCode(() -> ticketManagerService.getBookedTicketReceipt(BOOKING_ID, EMAIL_ADDRESS_2))
                .isInstanceOf(Exception.class)
                .hasMessage(ApplicationConstants.EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_get_ticket_receipt_by_booking_id() throws Exception {
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var ticketReceipt = bookings.values().stream().toList().get(0);

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        var receipt = ticketManagerService.getBookedTicketReceipt(BOOKING_ID, EMAIL_ADDRESS);

        assertThat(receipt).isNotNull();
        assertThat(receipt.getBoardingStation()).isEqualTo(ticketReceipt.getBoardingStation());
        assertThat(receipt.getDestinationStation()).isEqualTo(ticketReceipt.getDestinationStation());
        assertThat(receipt.getSeatNumber()).isEqualTo(ticketReceipt.getSeatNumber());
        assertThat(receipt.getSection()).isEqualTo(ticketReceipt.getSection());
        assertThat(receipt.getPassengerDto().getEmailAddress())
                .isEqualTo(ticketReceipt.getPassengerDto().getEmailAddress());
        assertThat(receipt.getPassengerDto().getFirstName())
                .isEqualTo(ticketReceipt.getPassengerDto().getFirstName());
        assertThat(receipt.getPassengerDto().getLastName())
                .isEqualTo(ticketReceipt.getPassengerDto().getLastName());

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_get_bookings_by_section_id() throws Exception {
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookedSeats = Collections.singletonList(bookings.get(BOOKING_ID));

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);
        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(ticketMapper.mapTicketReceiptToBookingsDto(bookedSeats))
                .willReturn(Collections.singletonList(getSectionBookingsDtoMock(bookedSeats.get(0))));

        var sectionBookingList = ticketManagerService.getBookingsBySectionId(1);

        assertThat(sectionBookingList).hasSize(1);

        given(ticketMapper.mapTicketReceiptToBookingsDto(Collections.emptyList())).willReturn(Collections.emptyList());

        sectionBookingList = ticketManagerService.getBookingsBySectionId(2);

        assertThat(sectionBookingList).isEmpty();

        verify(trainSeatManager, times(2)).getTrainSeats();
        verify(trainSeatManager, times(2)).getSeatBookings();
        verify(ticketMapper).mapTicketReceiptToBookingsDto(bookedSeats);
        verify(ticketMapper).mapTicketReceiptToBookingsDto(Collections.emptyList());
    }

    @Test
    void should_throw_exception_when_booking_not_found_for_given_booking_id_for_deletion() {
        String bookingId = "2045678900";
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        assertThatCode(() -> ticketManagerService.deleteBooking(bookingId, EMAIL_ADDRESS))
                .isInstanceOf(Exception.class)
                .hasMessage(ApplicationConstants.NO_BOOKING_FOUND_ERROR_MESSAGE + bookingId);

        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_given_email_address_not_matching_for_deletion() {
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        assertThatCode(() -> ticketManagerService.deleteBooking(BOOKING_ID, EMAIL_ADDRESS_2))
                .isInstanceOf(Exception.class)
                .hasMessage(ApplicationConstants.EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_delete_booking_by_id() throws Exception {
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);
        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());

        ticketManagerService.deleteBooking(BOOKING_ID, EMAIL_ADDRESS);

        verify(trainSeatManager).getSeatBookings();
        verify(trainSeatManager).getTrainSeats();
    }

    @Test
    void should_throw_exception_when_passenger_wants_to_modify_seat_when_there_is_no_other_seat_available() {
        var seats = getTrainSeatsMapMockWithoutEmptySeats();

        given(trainSeatManager.getTrainSeats()).willReturn(seats);

        assertThatCode(() -> ticketManagerService.modifyPassengerSeat("20240802210508", EMAIL_ADDRESS))
                .isInstanceOf(Exception.class)
                .hasMessage(ApplicationConstants.NO_SEATS_AVAILABLE_FOR_MODIFICATION_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
    }

    @Test
    void should_throw_exception_when_email_address_not_matches_for_modification() {
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        assertThatCode(() -> ticketManagerService.modifyPassengerSeat("20240802210508", EMAIL_ADDRESS_2))
                .isInstanceOf(Exception.class)
                .hasMessage(ApplicationConstants.EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_execute_passenger_seat_modification_request() throws Exception {
        var availableSeats = getTrainSeatsWithAvailableSeatsMock();
        var seatBookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(availableSeats);
        given(trainSeatManager.getSeatBookings()).willReturn(seatBookings);

        var modificationReceipt = ticketManagerService
                .modifyPassengerSeat("20240802210508", EMAIL_ADDRESS);

        assertThat(modificationReceipt).isNotNull();
        assertThat(modificationReceipt.getSeatNumber()).isEqualTo(2);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }
}