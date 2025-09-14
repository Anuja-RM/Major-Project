package com.Major.majorProject.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalTime;

@Getter
@Setter
public class SlotDetails {
    private Long id;
    private LocalTime startTime;
    private LocalTime endTime;
    private String status;
    private long cafeId;
}