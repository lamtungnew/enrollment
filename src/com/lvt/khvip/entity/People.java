package com.lvt.khvip.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import com.lvt.khvip.constant.Constants;

@SessionScoped
@ManagedBean
public class People implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private String peopleId;
	private String fullName;
	private String dateOfBirth;
	private LocalDate birthday;
	private String gender;
	private String customerType;
	private String lastCheckinTime;
	private String imagePath;
	private int groupId;
	private String mobilePhone;
	private String groupName;
	private int customerTypeId;
	private int status;
	private Date dateOfBirth2;
	private Integer ruleId;
	
	public int getId() {
		return id;
	}

	public Date getDateOfBirth2() {
		return dateOfBirth2;
	}

	public void setDateOfBirth2(Date dateOfBirth2) {
		this.dateOfBirth2 = dateOfBirth2;
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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDateOfBirth() {
		if (dateOfBirth != null && dateOfBirth.isEmpty()) {
			dateOfBirth = null;
		}
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		try {
			birthday = LocalDate.parse(dateOfBirth, DateTimeFormatter.ISO_DATE);
		} catch (Exception ex) {
		}
		this.dateOfBirth = dateOfBirth;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getCustomerType() {
		return customerType;
	}

	public void setCustomerType(String customerType) {
		this.customerType = customerType;
	}

	public String getLastCheckinTime() {
		return lastCheckinTime;
	}

	public void setLastCheckinTime(String lastCheckinTime) {
		this.lastCheckinTime = lastCheckinTime;
	}

	public String getImagePath() {
		return imagePath;
	}
	
	public String getImagePathNoHost() {
		return imagePath.replace(Constants.URL_IMAGE, "");
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public int getCustomerTypeId() {
		return customerTypeId;
	}

	public void setCustomerTypeId(int customerTypeId) {
		this.customerTypeId = customerTypeId;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
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

	public Integer getRuleId() {
		return ruleId;
	}

	public void setRuleId(Integer ruleId) {
		this.ruleId = ruleId;
	}
}
