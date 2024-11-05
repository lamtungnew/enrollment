/**
 * 
 */
package com.lvt.khvip.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class Detection implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String peopleId;
	private String capturedImagePath;
	private String fullName;
	private String gender;
	private String dateOfBirth;
	private LocalDate birthday;
	private String createdTime;
	private String customerType;
	private int cameraId;
	private String cameraName;
	private String groupName;
	private String mobilePhone;
	private int customerTypeId;
	private int groupId;
	private String livenessStatus;
	private String fullNameMark;
	private String dayFirstTime;
	private String dayNoonTime;

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

	public String getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(String createdTime) {
		this.createdTime = createdTime;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
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

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public int getCustomerTypeId() {
		return customerTypeId;
	}

	public void setCustomerTypeId(int customerTypeId) {
		this.customerTypeId = customerTypeId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getLivenessStatus() {
		return livenessStatus;
	}

	public void setLivenessStatus(String livenessStatus) {
		this.livenessStatus = livenessStatus;
	}

	public String getDayFirstTime() {
		return dayFirstTime;
	}

	public void setDayFirstTime(String dayFirstTime) {
		this.dayFirstTime = dayFirstTime;
	}

	public String getDayNoonTime() {
		return dayNoonTime;
	}

	public void setDayNoonTime(String dayNoonTime) {
		this.dayNoonTime = dayNoonTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}
	
}
