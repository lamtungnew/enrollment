package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftListData implements Serializable {
    private Long autoid;
    private String peopleId;
    private String peopleFullName;
    private Date createdAt;
    private Date startDate;
    private Date expireDate;
    private Date dateOfBirth;
    private String createdBy;
    private Integer shiftId;
    private String status;
    private String shiftTarget;
    private String shiftCode;
    private String shiftName;
    private String mobilePhone;
    private String gender;
    private String fullName;
    private String cusType;
    private Integer groupId;
    private String groupCode;
    private String groupName;
    private Integer gid;

    private Integer depId;
    private String depCode;
    private String depName;

    private String import_id;
    
    private String error;
    private String message;
    private String errorCode;
    private String errorMessage;

    private ShiftConfigDto shiftConfig;
}
