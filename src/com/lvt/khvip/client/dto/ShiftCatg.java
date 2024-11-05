package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftCatg implements Serializable {
    private Integer autoid;
    private String code;
    private String name;
    private String startTimeFrom;
    private String endTimeFrom;
    private String description;
    private String startTimeTo;
    private String endTimeTo;
    private String workingDayType;
}
