package com.cloudbees.train.server.service;

import com.cloudbees.train.SectionBookingResponse;
import com.cloudbees.train.TicketReceiptResponse;
import com.cloudbees.train.server.mapper.TicketMapper;
import com.cloudbees.train.server.persistence.TrainSeatManager;
import com.google.protobuf.Empty;
import io.grpc.StatusException;
import io.grpc.internal.testing.StreamRecorder;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static com.cloudbees.train.server.constants.ApplicationConstants.*;
import static com.cloudbees.train.server.factory.TicketFactory.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketManagerServiceImplTest {
    @Mock
    private TrainSeatManager trainSeatManager;
    @Mock
    private TicketMapper ticketMapper;

    @InjectMocks
    private TicketManagerServiceImpl ticketManagerServiceImpl;

    private StreamRecorder<TicketReceiptResponse> ticketReceiptResponseStreamObserver;
    private StreamRecorder<Empty> ticketDeleteResponseStreamObserver;
    private StreamRecorder<SectionBookingResponse> sectionBookingResponseStreamRecorder;

    @BeforeEach
    void createResponseObservers() {
        ticketReceiptResponseStreamObserver = StreamRecorder.create();
        ticketDeleteResponseStreamObserver = StreamRecorder.create();
        sectionBookingResponseStreamRecorder = StreamRecorder.create();
    }

    @Test
    void should_throw_exception_when_ticket_purchase_request_is_null() {
        ticketManagerServiceImpl.bookTicket(null, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(PURCHASE_REQUEST_ERROR_MESSAGE);
    }

    @Test
    void should_throw_exception_when_no_seats_are_available_for_booking() {
        var purchaseRequest = getTicketPurchaseRequestDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsMapMockWithoutEmptySeats());

        ticketManagerServiceImpl.bookTicket(purchaseRequest, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(NO_SEATS_AVAILABLE_FOR_NEW_BOOKING);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_passenger_details_has_empty_values() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(StringUtils.EMPTY, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());

        ticketManagerServiceImpl.bookTicket(purchaseDto, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE);

        var purchaseDto_1 = getTicketPurchaseRequestDtoMock(FIRST_NAME,
                StringUtils.EMPTY, EMAIL_ADDRESS);

        ticketManagerServiceImpl.bookTicket(purchaseDto_1, ticketReceiptResponseStreamObserver);

        error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE);

        var purchaseDto_2 = getTicketPurchaseRequestDtoMock(FIRST_NAME,
                LAST_NAME, StringUtils.EMPTY);

        ticketManagerServiceImpl.bookTicket(purchaseDto_2, ticketReceiptResponseStreamObserver);

        error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE);

        verify(trainSeatManager, times(3)).getTrainSeats();
        verify(trainSeatManager, times(3)).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_provided_passenger_email_is_not_valid() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(FIRST_NAME,
                LAST_NAME, "azar.gmail.com");

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());

        ticketManagerServiceImpl.bookTicket(purchaseDto, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(EMAIL_ADDRESS_FORMAT_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_passenger_exist_with_same_name_and_email_address() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.bookTicket(purchaseDto, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(PASSENGER_EXIST_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_execute_ticket_booking() {
        var purchaseDto = getTicketPurchaseRequestDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS_2);
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var ticketReceipt = getTicketReceiptDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS,
                2, SECTION_A);

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(ticketMapper.mapTicketReceiptForPurchase(any(), any(), any())).willReturn(ticketReceipt);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.bookTicket(purchaseDto, ticketReceiptResponseStreamObserver);

        var result = ticketReceiptResponseStreamObserver.getValues();
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);

        assertThat(result.get(0).getSeatNumber()).isEqualTo(2);
        assertThat(result.get(0).getSection()).isEqualTo(SECTION_A);

        verify(trainSeatManager).getSeatBookings();
        verify(trainSeatManager).getTrainSeats();
        verify(ticketMapper).mapTicketReceiptForPurchase(any(), any(), any());
    }

    @Test
    void should_throw_exception_when_booking_request_is_null() {
        ticketManagerServiceImpl.getBookedTicketReceipt(null, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(BOOKED_REQUEST_ERROR_MESSAGE);
    }

    @Test
    void should_throw_exception_to_get_receipt_by_booking_id() {
        var bookingRequest = getBookingRequestMock("2045678900", EMAIL_ADDRESS);
        var seats = getTrainSeatsWithAvailableSeatsMock();

        given(trainSeatManager.getTrainSeats()).willReturn(seats);

        ticketManagerServiceImpl.getBookedTicketReceipt(bookingRequest, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(NO_BOOKING_FOUND_ERROR_MESSAGE + bookingRequest.getBookingId());

        verify(trainSeatManager).getTrainSeats();
    }

    @Test
    void should_throw_error_when_email_address_not_valid_to_get_already_booked_receipt() {
        var bookingRequest = getBookingRequestMock(BOOKING_ID, EMAIL_ADDRESS_2);
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.getBookedTicketReceipt(bookingRequest, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(EMAIL_ADDRESS_NOT_MATCHING);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_get_ticket_receipt_by_booking_id() {
        var bookingRequest = getBookingRequestMock(BOOKING_ID, EMAIL_ADDRESS_2);
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var ticketReceipt = bookings.values().stream().toList().get(0);

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.getBookedTicketReceipt(bookingRequest, ticketReceiptResponseStreamObserver);
        var response = ticketReceiptResponseStreamObserver.getValues();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        var result = response.get(0);

        assertThat(result).isNotNull();
        assertThat(result.getBoardingStation()).isEqualTo(ticketReceipt.getBoardingStation());
        assertThat(result.getDestinationStation()).isEqualTo(ticketReceipt.getDestinationStation());
        assertThat(result.getSeatNumber()).isEqualTo(ticketReceipt.getSeatNumber());
        assertThat(result.getSection()).isEqualTo(ticketReceipt.getSection());
        assertThat(result.getPassenger().getEmailAddress())
                .isEqualTo(ticketReceipt.getPassenger().getEmailAddress());
        assertThat(result.getPassenger().getFirstName())
                .isEqualTo(ticketReceipt.getPassenger().getFirstName());
        assertThat(result.getPassenger().getLastName())
                .isEqualTo(ticketReceipt.getPassenger().getLastName());

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_section_request_is_null() {
        ticketManagerServiceImpl.getBookingsBySection(null, sectionBookingResponseStreamRecorder);

        var error = sectionBookingResponseStreamRecorder.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(SECTION_REQUEST_ERROR_MESSAGE);
    }

    @Test
    void should_get_bookings_by_section_id() {
        var sectionRequest = getSectionRequestMock(1);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookedSeats = Collections.singletonList(bookings.get(BOOKING_ID));
        var response = getSectionBookingResponse();

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);
        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(ticketMapper.mapTicketReceiptToBookingsDto(bookedSeats))
                .willReturn(response);

        ticketManagerServiceImpl.getBookingsBySection(sectionRequest, sectionBookingResponseStreamRecorder);

        var result = sectionBookingResponseStreamRecorder.getValues();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSectionBookingList()).hasSize(1);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
        verify(ticketMapper).mapTicketReceiptToBookingsDto(bookedSeats);
    }

    @Test
    void should_return_empty_section_bookings() {
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var seats = getTrainSeatsWithAvailableSeatsMock();

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);
        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(ticketMapper.mapTicketReceiptToBookingsDto(Collections.emptyList()))
                .willReturn(SectionBookingResponse.newBuilder().addAllSectionBooking(Collections.emptyList()).build());

        ticketManagerServiceImpl.getBookingsBySection(getSectionRequestMock(2), sectionBookingResponseStreamRecorder);

        var emptyResult = sectionBookingResponseStreamRecorder.getValues();

        assertThat(emptyResult).isNotNull();
        assertThat(emptyResult).hasSize(1);
        assertThat(emptyResult.get(0).getSectionBookingList()).isEmpty();

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_delete_booking_request_is_null() {
        ticketManagerServiceImpl.deleteBooking(null, ticketDeleteResponseStreamObserver);

        var error = ticketDeleteResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(BOOKED_REQUEST_ERROR_MESSAGE);
    }

    @Test
    void should_throw_exception_when_booking_not_found_for_given_booking_id_for_deletion() {
        var bookingRequest = getBookingRequestMock("2045678900", EMAIL_ADDRESS);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.deleteBooking(bookingRequest, ticketDeleteResponseStreamObserver);

        var error = ticketDeleteResponseStreamObserver.getError();

        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(NO_BOOKING_FOUND_ERROR_MESSAGE + bookingRequest.getBookingId());

        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_given_email_address_not_matching_for_deletion() {
        var bookingRequest = getBookingRequestMock(BOOKING_ID, EMAIL_ADDRESS_2);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.deleteBooking(bookingRequest, ticketDeleteResponseStreamObserver);

        var error = ticketDeleteResponseStreamObserver.getError();

        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_delete_booking_by_id() {
        var bookingRequest = getBookingRequestMock(BOOKING_ID, EMAIL_ADDRESS);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);
        given(trainSeatManager.getTrainSeats()).willReturn(getTrainSeatsWithAvailableSeatsMock());

        ticketManagerServiceImpl.deleteBooking(bookingRequest, ticketDeleteResponseStreamObserver);

        assertThat(ticketDeleteResponseStreamObserver.getError()).isNull();

        verify(trainSeatManager).getSeatBookings();
        verify(trainSeatManager).getTrainSeats();
    }

    @Test
    void should_throw_exception_when_modification_booking_request_is_null() {
        ticketManagerServiceImpl.modifyPassengerSeat(null, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();
        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(BOOKED_REQUEST_ERROR_MESSAGE);
    }

    @Test
    void should_throw_exception_when_booking_not_found_for_given_booking_id_for_seat_modification() {
        var bookingRequest = getBookingRequestMock("2045678900", EMAIL_ADDRESS);
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.modifyPassengerSeat(bookingRequest, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();

        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(NO_BOOKING_FOUND_ERROR_MESSAGE + bookingRequest.getBookingId());

        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_throw_exception_when_passenger_wants_to_modify_seat_when_there_is_no_other_seat_available() {
        var bookingRequest = getBookingRequestMock(BOOKING_ID, EMAIL_ADDRESS);
        var seats = getTrainSeatsMapMockWithoutEmptySeats();

        given(trainSeatManager.getTrainSeats()).willReturn(seats);

        ticketManagerServiceImpl.modifyPassengerSeat(bookingRequest, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();

        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(NO_SEATS_AVAILABLE_FOR_MODIFICATION_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
    }

    @Test
    void should_throw_exception_when_email_address_not_matches_for_modification() {
        var bookingRequest = getBookingRequestMock(BOOKING_ID, EMAIL_ADDRESS_2);
        var seats = getTrainSeatsWithAvailableSeatsMock();
        var bookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);

        given(trainSeatManager.getTrainSeats()).willReturn(seats);
        given(trainSeatManager.getSeatBookings()).willReturn(bookings);

        ticketManagerServiceImpl.modifyPassengerSeat(bookingRequest, ticketReceiptResponseStreamObserver);

        var error = ticketReceiptResponseStreamObserver.getError();

        assertThat(error).isInstanceOf(StatusException.class);
        assertThat(error).hasMessageContaining(EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }

    @Test
    void should_execute_passenger_seat_modification_request() {
        var bookingRequest = getBookingRequestMock(BOOKING_ID, EMAIL_ADDRESS);
        var availableSeats = getTrainSeatsWithAvailableSeatsMock();
        var seatBookings = getTrainSeatBookingsMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS);
        var newModificationReceipt =
                createNewTicketReceiptFromExisting(seatBookings.get(BOOKING_ID), 2, SECTION_A);

        given(trainSeatManager.getTrainSeats()).willReturn(availableSeats);
        given(trainSeatManager.getSeatBookings()).willReturn(seatBookings);
        given(ticketMapper.createNewTicketReceiptResponseFromExistOne(seatBookings.get(BOOKING_ID), 2, SECTION_A))
                .willReturn(newModificationReceipt);

        ticketManagerServiceImpl.modifyPassengerSeat(bookingRequest, ticketReceiptResponseStreamObserver);

        var response = ticketReceiptResponseStreamObserver.getValues();

        assertThat(response).isNotNull();
        assertThat(response).hasSize(1);

        var result = response.get(0);

        assertThat(result).isNotNull();
        assertThat(result.getSeatNumber()).isEqualTo(2);

        verify(trainSeatManager).getTrainSeats();
        verify(trainSeatManager).getSeatBookings();
    }
}