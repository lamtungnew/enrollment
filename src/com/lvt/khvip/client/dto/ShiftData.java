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
public class ShiftData implements Serializable {
    private Long autoid;
    private String peopleId;
    private Date createdAt;
    private Date startDate;
    private Date expireDate;
    private String createdBy;
    private Integer shiftId;
    private String status;
    private String shiftTarget;
    private Integer groupId;
    private Integer gid;
}
