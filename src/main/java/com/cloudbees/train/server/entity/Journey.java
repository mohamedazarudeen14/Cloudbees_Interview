package com.cloudbees.train.server.entity;

import lombok.Builder;
import lombok.Getter;

import java.util.Objects;

@Builder
@Getter
public class Journey {
    private String from;
    private String to;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Journey journey)) return false;
        return from.equals(journey.from) && to.equals(journey.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
