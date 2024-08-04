package com.cloudbees.train.client.dto;


import com.cloudbees.train.BookingRequest;
import com.cloudbees.train.Passenger;
import com.cloudbees.train.SectionRequest;
import com.cloudbees.train.TicketPurchaseRequest;

public class ExecutionDto {
    public static TicketPurchaseRequest getTicketPurchaseRequest(String fromStation, String toStation, String firstName,
                                                                 String lastName, String emailAddress) {
        return TicketPurchaseRequest
                .newBuilder()
                .setTicketPrice(20d)
                .setBoardingStation(fromStation)
                .setDestinationStation(toStation)
                .setPassenger(getPassenger(firstName, lastName, emailAddress))
                .build();
    }

    public static Passenger getPassenger(String firstName, String lastName, String emailAddress) {
        return Passenger.newBuilder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmailAddress(emailAddress)
                .build();
    }

    public static BookingRequest getBookingRequest(String bookingId, String emailAddress) {
        return BookingRequest.newBuilder()
                .setBookingId(bookingId)
                .setEmailAddress(emailAddress)
                .build();
    }

    public static SectionRequest getSectionRequest(int sectionId) {
        return SectionRequest.newBuilder()
                .setSectionId(sectionId)
                .build();
    }
}
