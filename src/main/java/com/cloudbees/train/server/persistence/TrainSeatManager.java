package com.cloudbees.train.server.persistence;

import com.cloudbees.train.TicketReceiptResponse;
import com.cloudbees.train.server.entity.Seat;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrainSeatManager {
    private static Map<Seat, String> trainSeats;
    private static Map<String, TicketReceiptResponse> seatBookings;

    public TrainSeatManager() {
        if (trainSeats == null) {
            trainSeats = new ConcurrentHashMap<>();
            seatBookings = new ConcurrentHashMap<>();
            initializeTrainSeats();
        }
    }

    public Map<Seat, String> getTrainSeats() {
        return trainSeats;
    }

    public Map<String, TicketReceiptResponse> getSeatBookings() {
        return seatBookings;
    }

    private static void initializeTrainSeats() {
        int totalSeats = 90;
        //Splitting the total seats equally in each two sections
        String sectionName = "SECTION A";
        int sectionId = 1;
        int seatNumber = 1;
        for (int i = 0; i < totalSeats; i++) {
            var seat = getSeat(sectionId, seatNumber, sectionName);
            trainSeats.put(seat, StringUtils.EMPTY);
            seatNumber++;

            if (((totalSeats / 2) - 1) == i) {
                sectionName = "SECTION B";
                sectionId = 2;
                seatNumber = 1;
            }
        }
    }

    private static Seat getSeat(int sectionId, int seatNumber, String sectionName) {
        return Seat.builder()
                .SectionId(sectionId)
                .seatNumber(seatNumber)
                .sectionName(sectionName)
                .build();
    }
}
