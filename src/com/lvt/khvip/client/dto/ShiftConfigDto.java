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
public class ShiftConfigDto implements Serializable {
    private Integer autoid;
    private String code;
    private String name;
    private Date startDate;
    private Date expireDate;
    private String description;
    private String workingDayType;
    private List<ShiftConfigDetailDto> shiftDetail;
    private List<ShiftConfigOrgDto> shiftOrg;
    private List<ShiftConfigDetailDto> oshiftDetail;
    private Integer depId;
    private Integer groupId;
    private String status;
    private String error;
    private String message;
    private boolean isEdit = false;
}
