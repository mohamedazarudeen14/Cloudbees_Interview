package com.cloudbees.train.client;

import com.cloudbees.train.TicketManagerServiceGrpc;
import com.cloudbees.train.TicketPurchaseRequest;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.Scanner;

import static com.cloudbees.train.client.dto.ExecutionDto.*;

public class TicketManagerClientImpl {
    ManagedChannel channel;
    TicketManagerServiceGrpc.TicketManagerServiceBlockingStub stub;

    public TicketManagerClientImpl() {
        channel = ManagedChannelBuilder.forAddress("localhost", 5003)
                .usePlaintext()
                .build();
        stub = TicketManagerServiceGrpc.newBlockingStub(channel);
    }
    public void selectionYourOption() {
        try {
            System.out.println("Enter you option");
            System.out.println("1. Book Ticket");
            System.out.println("2. Get Receipt by booking id");
            System.out.println("3. Get bookings by section id");
            System.out.println("4. Delete booking by booking id");
            System.out.println("5. Modify seat by booking id");

            Scanner in = new Scanner(System.in);

            switch (in.nextInt()) {
                case 1 -> bookTicket();
                case 2 -> getReceipt();
                case 3 -> getBookingsBySection();
                case 4 -> deleteBooking();
                case 5 -> modifySeat();
                default -> System.out.println("Entered Option not available");
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            selectionYourOption();
        }
    }

    public void bookTicket() {
        System.out.println("Enter boarding station");
        Scanner in = new Scanner(System.in);
        String fromStation = in.next();
        System.out.println("Enter destination station");
        String toStation = in.next();
        System.out.println("Enter passenger firstname");
        String firstName = in.next();
        System.out.println("Enter passenger lastname");
        String lastName = in.next();
        System.out.println("Enter passenger email address");
        String emailAddress= in.next();

        TicketPurchaseRequest ticketPurchaseRequest = getTicketPurchaseRequest(fromStation, toStation,
                firstName, lastName, emailAddress);

        var response = stub.bookTicket(ticketPurchaseRequest);
        System.out.println("Ticket booking response" + response);
    }

    private void getReceipt() {
        System.out.println("Enter booking id from your receipt");
        Scanner in = new Scanner(System.in);

        String bookingId = in.next();
        System.out.println("Enter given email address used while ticket booking");
        String emailAddress = in.next();

        var request = getBookingRequest(bookingId, emailAddress);

        var response = stub.getBookedTicketReceipt(request);
        System.out.println("Ticket receipt " + response);
    }

    private void getBookingsBySection() {
        System.out.println("Enter section id (number) to get bookings");
        Scanner in = new Scanner(System.in);

        int sectionId = in.nextInt();

        var request = getSectionRequest(sectionId);

        var response = stub.getBookingsBySection(request);
        if(response.getSectionBookingList().isEmpty()) {
            System.out.println("No bookings available for given section id " + sectionId);
        } else {
            System.out.println("Current section bookings " + response);
        }
    }

    private void deleteBooking() {
        System.out.println("Enter booking id from your receipt to delete your booking");
        Scanner in = new Scanner(System.in);

        String bookingId = in.next();
        System.out.println("Enter given email address used while ticket booking");
        String emailAddress = in.next();

        var request = getBookingRequest(bookingId, emailAddress);

        var response = stub.deleteBooking(request);
        System.out.println("Booking deleted for given booking id " + response + bookingId);
    }

    private void modifySeat() {
        System.out.println("Enter booking id from your receipt");
        Scanner in = new Scanner(System.in);

        String bookingId = in.next();
        System.out.println("Enter given email address used while ticket booking");
        String emailAddress = in.next();

        var request = getBookingRequest(bookingId, emailAddress);

        var response = stub.modifyPassengerSeat(request);
        System.out.println("Modified seat details " + response);
    }
}
