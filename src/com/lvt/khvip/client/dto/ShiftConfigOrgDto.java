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
public class ShiftConfigOrgDto implements Serializable {
    private Integer autoid;
    private Integer shiftId;
    private Integer groupId;
    private String shiftCode;
    private Integer orgId;
    private String orgName;
    private String orgCode;
    private String status;
    private String groupName;
    private String groupCode;
}
