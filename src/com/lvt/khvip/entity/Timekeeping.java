package com.lvt.khvip.entity;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@SessionScoped
@ManagedBean
public class Timekeeping {

	private String peopleId;
	private String fullName;
	private String checkIn;
	private String checkOut;
	private String imagePath;
	private String dateTimeKeeping;
	private String totalWork;
	private String groupName;
	private String noonTime;
	private String decription;
	private String morningLate;
	private String earlyLeave;

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

	public String getCheckIn() {
		return checkIn;
	}

	public void setCheckIn(String checkIn) {
		this.checkIn = checkIn;
	}

	public String getCheckOut() {
		return checkOut;
	}

	public void setCheckOut(String checkOut) {
		this.checkOut = checkOut;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getDateTimeKeeping() {
		return dateTimeKeeping;
	}

	public void setDateTimeKeeping(String dateTimeKeeping) {
		this.dateTimeKeeping = dateTimeKeeping;
	}

	public String getTotalWork() {
		return totalWork;
	}

	public void setTotalWork(String totalWork) {
		this.totalWork = totalWork;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getNoonTime() {
		return noonTime;
	}

	public void setNoonTime(String noonTime) {
		this.noonTime = noonTime;
	}

	public String getDecription() {
		return decription;
	}

	public void setDecription(String decription) {
		this.decription = decription;
	}

	public String getMorningLate() {
		if (morningLate == null) {
			return morningLate;
		}
		if (Integer.parseInt(morningLate) > 0) {
			return "";
		}
		return morningLate.replace("-", "");
	}

	public void setMorningLate(String morningLate) {
		this.morningLate = morningLate;
	}

	public String getEarlyLeave() {
		if (morningLate == null) {
			return morningLate;
		}
		if (Integer.parseInt(earlyLeave) < 0) {
			return "";
		}
		return earlyLeave.replace("-", "");
	}

	public void setEarlyLeave(String earlyLeave) {
		this.earlyLeave = earlyLeave;
	}
}
