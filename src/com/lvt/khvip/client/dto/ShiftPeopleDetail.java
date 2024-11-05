package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftPeopleDetail implements Serializable {
    private Integer depId;
    private Integer groupId;
    private String peopleId;

    private Employee people;
    private Integer shiftId;
    private List<ShiftConfigDto> shifts;

    private String errorCode;
    private String errorMessage;
}
