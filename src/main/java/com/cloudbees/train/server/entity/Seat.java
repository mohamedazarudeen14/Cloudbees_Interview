package com.cloudbees.train.server.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class Seat {
    private int SectionId;
    private String sectionName;
    private int seatNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Seat seat)) return false;
        return SectionId == seat.SectionId && seatNumber == seat.seatNumber && sectionName.equals(seat.sectionName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(SectionId, sectionName, seatNumber);
    }
}
