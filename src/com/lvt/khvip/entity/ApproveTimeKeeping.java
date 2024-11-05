package com.lvt.khvip.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class ApproveTimeKeeping {
    private List<Integer> approvalIds;
    private Integer state;
    private String approvedBy;
    private String approvalByLevel2;
    private Date approvedAt;
    private Date approvedAtLevel2;

}
