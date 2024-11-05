package com.lvt.khvip.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import lombok.Data;

@ManagedBean
@RequestScoped
@Data
public class CapturedImages implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String peopleId;
	private String capturedImagePath;
	private String fullName;
	private String gender;
	private String dateOfBirth;
	private LocalDate birthday;
	private int responseTime;
	private String recognizationStatus;
	private String responseRaw;
	private String customerType;
	private String createdTime;
	private int cameraId;
	private String cameraName;
	private String groupName;
	private String mobilePhone;
	private int status;
	private int groupId;
	private int customerTypeId;
	private String livenessStatus;
	private String fullNameMark;

	public String getFullNameMark() {
		if (fullName != null && "ERROR".equals(livenessStatus)) {
			return fullName + " *";
		}
		return fullName;
	}

	public void setFullNameMark(String fullNameMark) {
		this.fullNameMark = fullNameMark;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPeopleId() {
		return peopleId;
	}

	public void setPeopleId(String peopleId) {
		this.peopleId = peopleId;
	}

	public String getCapturedImagePath() {
		return capturedImagePath;
	}

	public void setCapturedImagePath(String capturedImagePath) {
		this.capturedImagePath = capturedImagePath;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		try {
			birthday = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_DATE);
		} catch (Exception ex) {
		}
		this.dateOfBirth = dateOfBirth;
	}

	public int getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}

	public String getRecognizationStatus() {
		return recognizationStatus;
	}

	public void setRecognizationStatus(String recognizationStatus) {
		this.recognizationStatus = recognizationStatus;
	}

	public String getResponseRaw() {
		return responseRaw;
	}

	public void setResponseRaw(String responseRaw) {
		this.responseRaw = responseRaw;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public int getCameraId() {
		return cameraId;
	}

	public void setCameraId(int cameraId) {
		this.cameraId = cameraId;
	}

	public String getCameraName() {
		return cameraName;
	}

	public void setCameraName(String cameraName) {
		this.cameraName = cameraName;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
