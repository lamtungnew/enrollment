package com.lvt.khvip.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.io.Serializable;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShiftConfigDetailDto implements Serializable {
    private Integer autoid;
    private String shiftCode;
    private Integer shiftId;
    private String day;
    private String dayName;
    private String workingStartTime;
    private String workingEndTime;
    @JsonIgnore
    private LocalTime workingStartTimeLT;
    @JsonIgnore
    private LocalTime workingEndTimeLT;
    private String breakStartTime;
    private String breakEndTime;
    @JsonIgnore
    private LocalTime breakStartTimeLT;
    @JsonIgnore
    private LocalTime breakEndTimeLT;
    private Boolean isOverTime;
    private String status;
    private String shiftMiddleStart;
    private String shiftMiddleEnd;
    @JsonIgnore
    private LocalTime shiftMiddleStartLT;
    @JsonIgnore
    private LocalTime shiftMiddleEndLT;
    private boolean isOverTimeError = false;
    private String error;
    private String message;

    public Integer getDayInt() {
        return day != null ? Integer.valueOf(day) : null;
    }

}
