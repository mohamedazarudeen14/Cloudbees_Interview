package com.cloudbees.train.persistence;

import com.cloudbees.train.dto.TicketReceiptDto;
import com.cloudbees.train.entity.Seat;
import com.cloudbees.train.enums.TrainSection;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TrainSeatManager {
    private static Map<Seat, String> trainSeats;
    private static Map<String, TicketReceiptDto> seatBookings;

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

    public Map<String, TicketReceiptDto> getSeatBookings() {
        return seatBookings;
    }

    private static void initializeTrainSeats() {
        int totalSeats = 90;
        //Splitting the total seats equally in each two sections
        TrainSection trainSection = TrainSection.SECTION_A;
        int seatNumber = 1;
        for (int i = 0; i < totalSeats; i++) {
            var seat = getSeat(trainSection.getSectionId(), seatNumber);
            trainSeats.put(seat, StringUtils.EMPTY);
            seatNumber++;

            if (((totalSeats / 2) - 1) == i) {
                trainSection = TrainSection.SECTION_B;
                seatNumber = 1;
            }
        }
    }

    private static Seat getSeat(int sectionId, int seatNumber) {
        return Seat.builder()
                .SectionId(sectionId)
                .seatNumber(seatNumber)
                .build();
    }
}
