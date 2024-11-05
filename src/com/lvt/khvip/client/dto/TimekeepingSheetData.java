package com.lvt.khvip.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lvt.khvip.entity.TimekeepingDate;
import lombok.*;
import org.primefaces.model.file.UploadedFile;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@ToString
@Getter
@Setter
public class TimekeepingSheetData implements Serializable {

    private Integer autoid;
    private String peopleId;
    private String description;
    private Date createdAt;
    private String createdBy;
    private String timeKeepingImage;
    private String state;
    private String stateValue;
    private String approvedBy;
    private Date approvedAt;
    private String supervisor;
    private String dateType;
    private String import_id;
    private String cusType;
    private Date fromDate;
    private Date toDate;
    private String keepingDateForSearch;
    private Date keepingDate;
    private Date keepingDateFrom;
    private Date keepingDateTo;
	private String mistake;
    private Date keepingDateTypeDate;
    private Boolean selectedCheckbox;

    private Date approvalAtLevel2;
    private String roleLevel1;
    private String roleLevel2;

    @JsonIgnore
	private UploadedFile uploadedImage;
    
	private String fullName;
	private String dateOfBirth;
	private LocalDate birthday;
	private String gender;
	private String customerType;
	private String lastCheckinTime;
	private String imagePath;
	private Integer groupId;
	private Integer orgId;
	private String mobilePhone;
	private String groupName;
	private Integer customerTypeId;
	private Integer status;
    private Integer approvalLevel;
    private String approvalByLevel2;
    private String approvedByName;
    private String approvalByLevel2Name;

    private List<TimekeepingDate> timeKeepingDate = new ArrayList();

	private String[] timeKeepingDateCbxValues;

	private Integer ruleId;

    private Integer pageNo;
    private Integer limit;
    private String orderField;
    private boolean orderType;
}
