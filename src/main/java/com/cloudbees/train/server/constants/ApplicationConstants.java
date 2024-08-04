package com.cloudbees.train.server.constants;

public class ApplicationConstants {
    public static final String PURCHASE_REQUEST_ERROR_MESSAGE =
            "Ticket purchase request not exist and cannot proceed further";
    public static final String BOOKED_REQUEST_ERROR_MESSAGE = "Booked request is null and cannot proceed further";
    public static final String SECTION_REQUEST_ERROR_MESSAGE = "Section request is null and cannot proceed further";
    public static final String JOURNEY_DETAILS_ERROR_MESSAGE = "Boarding and destination station details not provided";
    public static final String NO_SEATS_AVAILABLE_FOR_NEW_BOOKING = "No Seats Available for booking";
    public static final String PASSENGER_DETAILS_EMPTY_ERROR_MESSAGE =
            "Passenger firsName, lastName and emailAddress are mandatory for ticket booking";
    public static final String EMAIL_ADDRESS_FORMAT_ERROR_MESSAGE = "Provided email address is not in proper format";
    public static final String PASSENGER_EXIST_ERROR_MESSAGE =
            "Passenger with same name has already purchased ticket with same email address";
    public static final String NO_BOOKING_FOUND_ERROR_MESSAGE = "No booking found with given booking Id = ";
    public static final String NO_SEATS_AVAILABLE_FOR_MODIFICATION_ERROR_MESSAGE = "No Seats available for seat change";
    public static final String EMAIL_ADDRESS_NOT_MATCHING_ERROR_MESSAGE =
            "Passenger email address not matched for seat modification";
    public static final String EMAIL_ADDRESS_NOT_MATCHING =
            "Passenger email address not matching with given booking id";
    public static final double TICKET_COST = 20d;
}
