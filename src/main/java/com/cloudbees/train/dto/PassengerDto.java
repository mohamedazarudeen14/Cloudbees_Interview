package com.cloudbees.train.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PassengerDto {
    private String firstName;
    private String lastName;
    private String emailAddress;
}
