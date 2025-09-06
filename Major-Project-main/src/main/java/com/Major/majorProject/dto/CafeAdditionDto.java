package com.Major.majorProject.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat; // Added import

import java.time.LocalTime;

@Getter
@Setter
public class CafeAdditionDto {
    private long id;
    private String name;
    private String address;
    @DateTimeFormat(pattern = "HH:mm") // Added annotation
    private LocalTime openTime;
    @DateTimeFormat(pattern = "HH:mm") // Added annotation
    private LocalTime closeTime;
    private double hourlyRate;
}