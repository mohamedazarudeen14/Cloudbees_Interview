package com.cloudbees.train.server.factory;

import com.cloudbees.train.BookingRequest;
import com.cloudbees.train.Passenger;
import com.cloudbees.train.SectionBooking;
import com.cloudbees.train.SectionBookingResponse;
import com.cloudbees.train.SectionRequest;
import com.cloudbees.train.TicketPurchaseRequest;
import com.cloudbees.train.TicketReceiptResponse;
import com.cloudbees.train.server.entity.Seat;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class TicketFactory {
    public static final String BOOKING_ID = "20240802210508";
    public static final String FIRST_NAME = "MOHAMED";
    public static final String LAST_NAME = "AZARUDEEN";
    public static final String EMAIL_ADDRESS = "Mohamed@gmail.com";
    public static final String EMAIL_ADDRESS_2 = "coder@gmail.com";
    public static final String SECTION_A = "SECTION A";

    public static Passenger getPassengerDtoMock(String firstName, String lastName, String emailAddress) {
        return Passenger.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmailAddress(emailAddress)
                .build();
    }

    public static Map<Seat, String> getTrainSeatsMapMockWithoutEmptySeats() {
        Map<Seat, String> seats = new HashMap<>();
        seats.put(getSeatMock(1, 1), "20240802210508");
        seats.put(getSeatMock(2, 1), "20240802210511");

        return seats;
    }

    public static Map<Seat, String> getTrainSeatsWithAvailableSeatsMock() {
        Map<Seat, String> seats = new HashMap<>();
        seats.put(getSeatMock(1, 1), BOOKING_ID);
        seats.put(getSeatMock(2, 1), StringUtils.EMPTY);

        return seats;
    }

    public static Map<String, TicketReceiptResponse> getTrainSeatBookingsMock(String firstName, String lastName,
                                                                              String emailAddress) {
        Map<String, TicketReceiptResponse> seatBookings = new HashMap<>();
        seatBookings.put(BOOKING_ID, getTicketReceiptDtoMock(firstName, lastName, emailAddress, 1,
                SECTION_A));

        return seatBookings;
    }

    public static TicketPurchaseRequest getTicketPurchaseRequestDtoMock(String firstName, String lastName,
                                                                        String emailAddress) {
        return TicketPurchaseRequest.newBuilder()
                .setPassenger(getPassengerDtoMock(firstName, lastName, emailAddress))
                .setBoardingStation("London")
                .setDestinationStation("France")
                .setTicketPrice(20d)
                .build();
    }

    public static TicketReceiptResponse getTicketReceiptDtoMock(String firstName, String lastName, String emailAddress,
                                                                int seatNumber, String sectionName) {
        return TicketReceiptResponse.newBuilder()
                .setBookingId(BOOKING_ID)
                .setSeatNumber(seatNumber)
                .setSection(sectionName)
                .setPricePaid(20d)
                .setBoardingStation("London")
                .setDestinationStation("France")
                .setPassenger(getPassengerDtoMock(firstName, lastName, emailAddress))
                .build();
    }

    public static Seat getSeatMock(int seatNumber, int sectionId) {
        return Seat.builder()
                .seatNumber(seatNumber)
                .SectionId(sectionId)
                .sectionName("SECTION A")
                .build();
    }

    public static SectionBooking getSectionBookingsDtoMock(TicketReceiptResponse ticketReceiptDto) {
        return SectionBooking.newBuilder()
                .setSectionName(ticketReceiptDto.getSection())
                .setPassenger(ticketReceiptDto.getPassenger())
                .setSeatNumber(ticketReceiptDto.getSeatNumber())
                .build();
    }

    public static SectionBookingResponse getSectionBookingResponse() {
        var receipt =
                getTicketReceiptDtoMock(FIRST_NAME, LAST_NAME, EMAIL_ADDRESS, 1, SECTION_A);
        return SectionBookingResponse.newBuilder()
                .addSectionBooking(getSectionBookingsDtoMock(receipt))
                .build();
    }

    public static BookingRequest getBookingRequestMock(String bookingId, String emailAddress) {
        return BookingRequest.newBuilder()
                .setBookingId(bookingId)
                .setEmailAddress(emailAddress)
                .build();
    }

    public static SectionRequest getSectionRequestMock(int sectionId) {
        return SectionRequest.newBuilder()
                .setSectionId(sectionId)
                .build();
    }

    public static TicketReceiptResponse createNewTicketReceiptFromExisting(TicketReceiptResponse response,
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
}
