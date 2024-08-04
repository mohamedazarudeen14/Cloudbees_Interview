package com.cloudbees.train.server.service;

import com.cloudbees.train.BookingRequest;
import com.cloudbees.train.Passenger;
import com.cloudbees.train.SectionBookingResponse;
import com.cloudbees.train.SectionRequest;
import com.cloudbees.train.TicketManagerServiceGrpc;
import com.cloudbees.train.TicketPurchaseRequest;
import com.cloudbees.train.TicketReceiptResponse;
import com.cloudbees.train.server.entity.Seat;
import com.cloudbees.train.server.mapper.TicketMapper;
import com.cloudbees.train.server.persistence.TrainSeatManager;
import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.cloudbees.train.server.constants.ApplicationConstants.*;

@AllArgsConstructor
public class TicketManagerServiceImpl extends TicketManagerServiceGrpc.TicketManagerServiceImplBase {
    private final TrainSeatManager trainSeatManager;
    private final TicketMapper ticketMapper;

    @Override
    public void bookTicket(TicketPurchaseRequest request,
                           StreamObserver<TicketReceiptResponse> responseObserver) {
        if (request == null) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(PURCHASE_REQUEST_ERROR_MESSAGE)
                    .asException());
            return;
        }

        var seatsMap = trainSeatManager.getTrainSeats();
        var seatBookings = trainSeatManager.getSeatBookings();

        var passengerSeat = validateNewTicket(seatsMap, seatBookings, request, responseObserver);

        if (passengerSeat.isEmpty()) {
            return;
        }

        var ticketCost = getJourneyTicketCost(request.getBoardingStation(),
                request.getDestinationStation(), responseObserver);

        if (ticketCost.isEmpty()) {
            return;
        }

        var bookingId = getBookingId();
        var ticketReceipt = ticketMapper
                .mapTicketReceiptForPurchase(passengerSeat.get(), bookingId, request, ticketCost.get());
        seatsMap.replace(passengerSeat.get(), bookingId);
        seatBookings.put(bookingId, ticketReceipt);

        responseObserver.onNext(ticketReceipt);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookedTicketReceipt(BookingRequest request, StreamObserver<TicketReceiptResponse> responseObserver) {
        if (request == null) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(BOOKED_REQUEST_ERROR_MESSAGE)
                    .asException());
            return;
        }

        var seatBookings = trainSeatManager.getSeatBookings();
        var seats = trainSeatManager.getTrainSeats();

        var availableSeat = seats.keySet().stream()
                .filter(obj -> seats.get(obj).equals(request.getBookingId())).toList();

        if (availableSeat.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(NO_BOOKING_FOUND_ERROR_MESSAGE + request.getBookingId())
                    .asException());
            return;
        }

        var ticketReceipt = validatePassengerEmail(seatBookings,
                request.getBookingId(), request.getEmailAddress(), responseObserver);

        responseObserver.onNext(ticketReceipt);
        responseObserver.onCompleted();
    }

    @Override
    public void getBookingsBySection(SectionRequest request, StreamObserver<SectionBookingResponse> responseObserver) {
        if (request == null) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(SECTION_REQUEST_ERROR_MESSAGE)
                    .asException());
            return;
        }

        var seats = trainSeatManager.getTrainSeats();
        var seatBookings = trainSeatManager.getSeatBookings();

        var sectionBookings = seats.keySet().stream()
                .filter(obj -> obj.getSectionId() == request.getSectionId() && !StringUtils.isEmpty(seats.get(obj)))
                .map(seats::get).toList();

        var bookings = sectionBookings.stream().map(seatBookings::get).toList();
        var sectionBookingResponse = ticketMapper.mapTicketReceiptToBookingsDto(bookings);

        responseObserver.onNext(sectionBookingResponse);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBooking(BookingRequest request, StreamObserver<Empty> responseObserver) {
        if (request == null) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(BOOKED_REQUEST_ERROR_MESSAGE)
                    .asException());
            return;
        }
        var seatsMap = trainSeatManager.getTrainSeats();
        var bookings = trainSeatManager.getSeatBookings();


        var bookedSeat = getBookedSeatByBookingId(seatsMap, request.getBookingId());

        if (bookedSeat.isEmpty()) {
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(NO_BOOKING_FOUND_ERROR_MESSAGE + request.getBookingId())
                            .asException());
            return;
        }

        var booking = bookings.get(request.getBookingId());

        if (!booking.getPassenger().getEmailAddress().equalsIgnoreCase(request.getEmailAddress())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE)
                    .asException());
            return;
        }

        bookings.remove(booking.getBookingId());
        seatsMap.replace(bookedSeat.get(), StringUtils.EMPTY);

        responseObserver.onNext(Empty.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void modifyPassengerSeat(BookingRequest request, StreamObserver<TicketReceiptResponse> responseObserver) {
        if (request == null) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(BOOKED_REQUEST_ERROR_MESSAGE)
                    .asException());
            return;
        }

        var seatsMap = trainSeatManager.getTrainSeats();
        var bookings = trainSeatManager.getSeatBookings();

        var bookedSeatById = getBookedSeatByBookingId(seatsMap, request.getBookingId());

        if (bookedSeatById.isEmpty()) {
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(NO_BOOKING_FOUND_ERROR_MESSAGE + request.getBookingId())
                            .asException());
            return;
        }

        var availableSeat = seatsMap.keySet().stream()
                .filter(obj -> StringUtils.isEmpty(seatsMap.get(obj))).findFirst();

        if (availableSeat.isEmpty()) {
            responseObserver
                    .onError(Status.NOT_FOUND.withDescription(NO_SEATS_AVAILABLE_FOR_MODIFICATION_ERROR_MESSAGE)
                            .asException());
            return;
        }

        var booking = bookings.get(request.getBookingId());

        if (!booking.getPassenger().getEmailAddress().equalsIgnoreCase(request.getEmailAddress())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE)
                    .asException());
            return;
        }

        var availableSeatEntry = availableSeat.get();
        var newTicketReceipt = ticketMapper.createNewTicketReceiptResponseFromExistOne(booking,
                availableSeatEntry.getSeatNumber(), availableSeatEntry.getSectionName());

        bookings.replace(request.getBookingId(), newTicketReceipt);
        seatsMap.replace(bookedSeatById.get(), StringUtils.EMPTY);
        seatsMap.replace(availableSeat.get(), request.getBookingId());

        responseObserver.onNext(newTicketReceipt);
        responseObserver.onCompleted();
    }

    private Optional<Seat> getBookedSeatByBookingId(Map<Seat, String> seats, String bookingId) {
        return seats.keySet().stream()
                .filter(obj -> seats.get(obj).equals(bookingId)).findFirst();
    }

    private Optional<Seat> validateNewTicket(Map<Seat, String> seats, Map<String, TicketReceiptResponse> seatBookings,
                                             TicketPurchaseRequest request, StreamObserver<TicketReceiptResponse> responseObserver) {
        var availableSeat = seats.keySet().stream()
                .filter(obj -> StringUtils.isEmpty(seats.get(obj))).findFirst();

        if (availableSeat.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(NO_SEATS_AVAILABLE_FOR_NEW_BOOKING)
                    .asException());
            return Optional.empty();
        }

        if (StringUtils.isBlank(request.getBoardingStation()) || StringUtils.isBlank(request.getDestinationStation())) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(JOURNEY_DETAILS_ERROR_MESSAGE)
                    .asException());
            return Optional.empty();
        }

        if (!request.hasPassenger() || isPassengerDetailsNotExist(request.getPassenger())) {
            responseObserver.onError(Status.FAILED_PRECONDITION.withDescription(PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE)
                    .asException());
            return Optional.empty();
        }

        if (!isValidEmailAddress(request.getPassenger().getEmailAddress())) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(EMAIL_ADDRESS_FORMAT_ERROR_MESSAGE)
                    .asException());
            return Optional.empty();
        }

        var isPassengerExist = seatBookings.values().stream()
                .filter(obj -> validatePassengerIfAlreadyHasTicket(obj.getPassenger(), request.getPassenger()))
                .findFirst();

        if (isPassengerExist.isPresent()) {
            responseObserver.onError(Status.ALREADY_EXISTS.withDescription(PASSENGER_EXIST_ERROR_MESSAGE)
                    .asException());
            return Optional.empty();
        }

        return availableSeat;
    }

    private boolean validatePassengerIfAlreadyHasTicket(Passenger existingPassenger, Passenger newPassenger) {
        return existingPassenger.getEmailAddress().equalsIgnoreCase(newPassenger.getEmailAddress())
                && existingPassenger.getFirstName().equalsIgnoreCase(newPassenger.getFirstName())
                && existingPassenger.getLastName().equalsIgnoreCase(newPassenger.getLastName());
    }

    private boolean isPassengerDetailsNotExist(Passenger passenger) {
        return StringUtils.isBlank(passenger.getFirstName())
                || StringUtils.isBlank(passenger.getLastName())
                || StringUtils.isBlank(passenger.getEmailAddress());
    }

    private boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    private TicketReceiptResponse validatePassengerEmail(Map<String, TicketReceiptResponse> seatBookings,
                                                         String bookingId, String emailAddress,
                                                         StreamObserver<TicketReceiptResponse> responseObserver) {
        var booking = seatBookings.get(bookingId);

        if (!booking.getPassenger().getEmailAddress().equalsIgnoreCase(emailAddress)) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(EMAIL_ADDRESS_NOT_MATCHING)
                    .asException());
        }

        return booking;
    }

    private Optional<Double> getJourneyTicketCost(String from, String to,
                                                  StreamObserver<TicketReceiptResponse> responseObserver) {
        var travelJourneys = trainSeatManager.getTravelJourneys();

        var requiredJourney = travelJourneys.keySet().stream().filter(obj -> obj.getFrom().equalsIgnoreCase(from)
                && obj.getTo().equalsIgnoreCase(to)).findFirst();

        if (requiredJourney.isEmpty()) {
            responseObserver.onError(Status.NOT_FOUND.withDescription(TRAIN_JOURNEY_DETAILS_NOT_FOUND)
                    .asException());
            return Optional.empty();
        }

        return Optional.of(travelJourneys.get(requiredJourney.get()));
    }

    private String getBookingId() {
        var dateTime = LocalDateTime.now();
        return String.format("%1$d%2$d%3$d%4$d%5$d%6$d", dateTime.getYear(), dateTime.getMonthValue(),
                dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
    }
}
