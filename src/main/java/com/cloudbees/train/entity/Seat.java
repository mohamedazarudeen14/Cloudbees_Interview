package com.cloudbees.train.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class Seat {
    private int SectionId;
    private int seatNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return SectionId == seat.SectionId && seatNumber == seat.seatNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(SectionId, seatNumber);
    }
}
