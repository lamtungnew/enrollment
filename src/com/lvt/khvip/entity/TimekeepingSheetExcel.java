package com.lvt.khvip.entity;

import java.time.LocalDate;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class TimekeepingSheetExcel {

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
    private String dateType;
	private Date keepingDate;
	private String keepingDateString;
	private String peopleIdAndKeepingDate;
	private String mistake;

	private String fullName;
	private String dateOfBirth;
	private LocalDate birthday;
	private String gender;
	private String customerType;
	private String lastCheckinTime;
	private String imagePath;
	private Integer groupId;
	private String mobilePhone;
	private String groupName;
	private Integer customerTypeId;
	private Integer status;
	private Date dateOfBirth2;

	private String error;
	private String message;
	private String errorCode;
	private String errorMessage;
}
