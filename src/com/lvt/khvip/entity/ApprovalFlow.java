package com.lvt.khvip.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class ApprovalFlow implements Serializable {
    private Integer autoid;
    private Integer groupId;
    private Integer depId;
    private String approvalType;
    private Integer approvalLevel;
    private String approvalLevel1;
    private String approvalLevel2;
    private String approvalLevel3;
    private Date createdAt;
    private String createdBy;
    private Date modifiedAt;

    private String approvalLevel1PeopleId;
    private String approvalLevel2PeopleId;
    private String approvalLevel1Name;
    private String approvalLevel2Name;
}