package com.cloudbees.train.service;

import com.cloudbees.train.constants.ApplicationConstants;
import com.cloudbees.train.dto.PassengerDto;
import com.cloudbees.train.dto.SectionBookingsDto;
import com.cloudbees.train.dto.TicketPurchaseRequestDto;
import com.cloudbees.train.dto.TicketReceiptDto;
import com.cloudbees.train.entity.Seat;
import com.cloudbees.train.enums.TrainSection;
import com.cloudbees.train.mapper.TicketMapper;
import com.cloudbees.train.persistence.TrainSeatManager;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class TicketManagerService {
    private final TrainSeatManager trainSeatManager;
    private final TicketMapper ticketMapper;

    public TicketReceiptDto getTrainTicketReceipt(TicketPurchaseRequestDto dto) throws Exception {
        var seatsMap = trainSeatManager.getTrainSeats();
        var seatBookings = trainSeatManager.getSeatBookings();

        var passengerSeat = validateNewTicket(seatsMap, seatBookings, dto);
        var bookingId = getBookingId();
        var ticketReceipt = ticketMapper.mapTicketReceiptForPurchase(passengerSeat, bookingId, dto);
        seatsMap.replace(passengerSeat, bookingId);
        seatBookings.put(bookingId, ticketReceipt);

        return ticketReceipt;
    }

    public TicketReceiptDto getBookedTicketReceipt(String bookingId, String emailAddress) throws Exception {
        var seatBookings = trainSeatManager.getSeatBookings();
        var seats = trainSeatManager.getTrainSeats();

        getSeatDetailsByBookingId(seats, bookingId);

        return validatePassengerEmailBeforeAnyChanges(seatBookings, bookingId, emailAddress);
    }

    public List<SectionBookingsDto> getBookingsBySectionId(int sectionId) throws Exception {
        TrainSection section = TrainSection.from(sectionId);
        var seats = trainSeatManager.getTrainSeats();
        var seatBookings = trainSeatManager.getSeatBookings();

        var sectionBookings = seats.keySet().stream()
                .filter(obj -> obj.getSectionId() == section.getSectionId() && !StringUtils.isEmpty(seats.get(obj)))
                .map(seats::get).toList();

        var bookings = sectionBookings.stream().map(seatBookings::get).toList();

        return ticketMapper.mapTicketReceiptToBookingsDto(bookings);
    }

    public void deleteBooking(String bookingId, String emailAddress) throws Exception {
        var seatsMap = trainSeatManager.getTrainSeats();
        var bookings = trainSeatManager.getSeatBookings();

        var blockedSeat = getSeatDetailsByBookingId(seatsMap, bookingId);
        var booking = validatePassengerEmailBeforeAnyChanges(bookings, bookingId, emailAddress);

        bookings.remove(booking.getBookingId());
        seatsMap.replace(blockedSeat, StringUtils.EMPTY);
    }

    public TicketReceiptDto modifyPassengerSeat(String bookingId, String emailAddress) throws Exception {
        var seatsMap = trainSeatManager.getTrainSeats();
        var bookings = trainSeatManager.getSeatBookings();

        var bookedSeat = getSeatDetailsByBookingId(seatsMap, bookingId);
        var availableSeat = seatsMap.keySet().stream()
                .filter(obj -> StringUtils.isEmpty(seatsMap.get(obj))).findFirst();

        if (availableSeat.isEmpty()) {
            throw new Exception(ApplicationConstants.NO_SEATS_AVAILABLE_FOR_MODIFICATION_ERROR_MESSAGE);
        }

        var ticketReceipt = validatePassengerEmailBeforeAnyChanges(bookings, bookingId, emailAddress);

        ticketReceipt.setSeatNumber(availableSeat.get().getSeatNumber());
        ticketReceipt.setSection(TrainSection.from(availableSeat.get().getSectionId()).getSectionName());
        seatsMap.replace(bookedSeat, StringUtils.EMPTY);
        seatsMap.replace(availableSeat.get(), bookingId);

        return ticketReceipt;
    }

    private Seat validateNewTicket(Map<Seat, String> seats, Map<String, TicketReceiptDto> seatBookings,
                                   TicketPurchaseRequestDto dto) throws Exception {
        var availableSeat = seats.keySet().stream()
                .filter(obj -> StringUtils.isEmpty(seats.get(obj))).findFirst();

        if (availableSeat.isEmpty()) {
            throw new Exception(ApplicationConstants.NO_SEATS_AVAILABLE_FOR_NEW_BOOKING);
        }

        if (isPassengerDetailsNotExist(dto.getPassengerDto())) {
            throw new Exception(ApplicationConstants.PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE);
        }

        if (!isValidEmailAddress(dto.getPassengerDto().getEmailAddress())) {
            throw new Exception(ApplicationConstants.EMAIL_ADDRESS_FORMAT_ERROR_MESSAGE);
        }

        var isPassengerExist = seatBookings.values().stream()
                .filter(obj -> validatePassengerIfAlreadyHasTicket(obj.getPassengerDto(), dto.getPassengerDto()))
                .findFirst();

        if (isPassengerExist.isPresent()) {
            throw new Exception(ApplicationConstants.PASSENGER_EXIST_ERROR_MESSAGE);
        }

        return availableSeat.get();
    }

    private Seat getSeatDetailsByBookingId(Map<Seat, String> seats, String bookingId) throws Exception {
        var availableSeat = seats.keySet().stream().filter(obj -> seats.get(obj).equals(bookingId))
                .findFirst();

        if (availableSeat.isEmpty()) {
            throw new Exception(ApplicationConstants.NO_BOOKING_FOUND_ERROR_MESSAGE + bookingId);
        }

        return availableSeat.get();
    }

    private boolean validatePassengerIfAlreadyHasTicket(PassengerDto existingPassengerDto, PassengerDto newPassengerDto) {
        return existingPassengerDto.getEmailAddress().equalsIgnoreCase(newPassengerDto.getEmailAddress())
                && existingPassengerDto.getFirstName().equalsIgnoreCase(newPassengerDto.getFirstName())
                && existingPassengerDto.getLastName().equalsIgnoreCase(newPassengerDto.getLastName());
    }

    private boolean isPassengerDetailsNotExist(PassengerDto passengerDto) {
        return StringUtils.isEmpty(passengerDto.getFirstName())
                || StringUtils.isEmpty(passengerDto.getLastName())
                || StringUtils.isEmpty(passengerDto.getEmailAddress());
    }

    private boolean isValidEmailAddress(String email) {
        return EmailValidator.getInstance().isValid(email);
    }

    private TicketReceiptDto validatePassengerEmailBeforeAnyChanges(Map<String, TicketReceiptDto> seatBookings,
                                                                    String bookingId, String emailAddress) throws Exception {
        var booking = seatBookings.get(bookingId);

        if (!booking.getPassengerDto().getEmailAddress().equalsIgnoreCase(emailAddress)) {
            throw new Exception(ApplicationConstants.EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE);
        }

        return booking;
    }

    private String getBookingId() {
        var dateTime = LocalDateTime.now();
        return String.format("%1$d%2$d%3$d%4$d%5$d%6$d", dateTime.getYear(), dateTime.getMonthValue(),
                dateTime.getDayOfMonth(), dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());
    }
}
