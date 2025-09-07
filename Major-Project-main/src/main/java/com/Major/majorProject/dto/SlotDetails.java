package com.Major.majorProject.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SlotDetails {
    private String startTime;
    private String endTime;
    private String status;//open or closed
    private long cafeId;
}