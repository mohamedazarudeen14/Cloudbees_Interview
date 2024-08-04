package com.cloudbees.train.server.persistence;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TrainSeatManagerTest {
    @InjectMocks
    private TrainSeatManager trainSeatManager;

    @Test
    void should_validate_train_seats_count() {
        var seats = trainSeatManager.getTrainSeats();
        var seatBookings = trainSeatManager.getSeatBookings();
        assertThat(seats.size()).isEqualTo(90);
        assertThat(seatBookings.size()).isZero();

        var sectionASeats = seats.keySet().stream()
                .filter(obj -> obj.getSectionId() == 1);
        var sectionBSeats = seats.keySet().stream()
                .filter(obj -> obj.getSectionId() == 2);

        assertThat(sectionASeats).hasSize(45);
        assertThat(sectionBSeats).hasSize(45);
    }

    @Test
    void should_validate_train_journeys_count() {
        var travelJourneys = trainSeatManager.getTravelJourneys();

        assertThat(travelJourneys).hasSize(1);
    }
}