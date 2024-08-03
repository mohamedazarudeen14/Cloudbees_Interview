package com.cloudbees.train.factory;

import org.apache.commons.lang.StringUtils;
import com.cloudbees.train.dto.PassengerDto;
import com.cloudbees.train.dto.SectionBookingsDto;
import com.cloudbees.train.dto.TicketPurchaseRequestDto;
import com.cloudbees.train.dto.TicketReceiptDto;
import com.cloudbees.train.entity.Seat;
import com.cloudbees.train.enums.TrainSection;

import java.util.HashMap;
import java.util.Map;

public class TicketFactory {
    public static final String BOOKING_ID = "20240802210508";
    public static final String FIRST_NAME = "MOHAMED";
    public static final String LAST_NAME = "AZARUDEEN";
    public static final String EMAIL_ADDRESS = "Mohamed@gmail.com";
    public static final String EMAIL_ADDRESS_2 = "coder@gmail.com";

    public static PassengerDto getPassengerDtoMock(String firstName, String lastName, String emailAddress) {
        return PassengerDto.builder()
                .firstName(firstName)
                .lastName(lastName)
                .emailAddress(emailAddress)
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

    public static Map<String, TicketReceiptDto> getTrainSeatBookingsMock(String firstName, String lastName,
                                                                         String emailAddress) {
        Map<String, TicketReceiptDto> seatBookings = new HashMap<>();
        seatBookings.put(BOOKING_ID, getTicketReceiptDtoMock(firstName, lastName, emailAddress, 1,
                TrainSection.SECTION_A.getSectionName()));

        return seatBookings;
    }

    public static TicketPurchaseRequestDto getTicketPurchaseRequestDtoMock(String firstName, String lastName,
                                                                           String emailAddress) {
        return TicketPurchaseRequestDto.builder()
                .passengerDto(getPassengerDtoMock(firstName, lastName, emailAddress))
                .boardingStation("London")
                .destinationStation("France")
                .ticketPrice(20d)
                .build();
    }

    public static TicketReceiptDto getTicketReceiptDtoMock(String firstName, String lastName, String emailAddress,
                                                           int seatNumber, String sectionName) {
        return TicketReceiptDto.builder()
                .bookingId(BOOKING_ID)
                .seatNumber(seatNumber)
                .section(sectionName)
                .pricePaid(20d)
                .boardingStation("London")
                .destinationStation("France")
                .passengerDto(getPassengerDtoMock(firstName, lastName, emailAddress))
                .build();
    }

    public static Seat getSeatMock(int seatNumber, int sectionId) {
        return Seat.builder()
                .seatNumber(seatNumber)
                .SectionId(sectionId)
                .build();
    }

    public static SectionBookingsDto getSectionBookingsDtoMock(TicketReceiptDto ticketReceiptDto) {
        return SectionBookingsDto.builder()
                .sectionName(ticketReceiptDto.getSection())
                .passengerDto(ticketReceiptDto.getPassengerDto())
                .seatNumber(ticketReceiptDto.getSeatNumber())
                .build();
    }
}
