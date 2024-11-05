package com.lvt.khvip.controller;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.RoleConstant;
import com.lvt.khvip.dao.CustomerTypeDao;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.HistoryPeopleSignedUpDao;
import com.lvt.khvip.entity.CustomerType;
import com.lvt.khvip.entity.Detection;
import com.lvt.khvip.entity.Groups;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.User;
import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utils;

@ManagedBean
@ViewScoped
public class HistorySignedUpController implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(HistorySignedUpController.class);

	private int zoneSelected;
	private int customerTypeSelected;
	private List<Groups> zone;
	private List<CustomerType> customerTypeList;
	private Detection detection;
	private List<Detection> detectionList;
	private String peopleIdSearch;
	private String statusSearch;
	private Date fromDate;
    private Date toDate;
	
	public HistorySignedUpController() {
		Map<String, String> params =FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String detectionId = params.get("detectionId");
		if(!StringUtils.isEmpty(detectionId)) {
			detection = HistoryPeopleSignedUpDao.getPeopleSignedUpById(Integer.parseInt(detectionId));
			zoneSelected = detection.getGroupId();
			customerTypeSelected = detection.getCustomerTypeId();
			
			if (RoleConstant.ADMIN.equals(User.isUserInRoles())) {
				GroupsDao groupsDao = new GroupsDao();
				zone = groupsDao.getGroupList();
				CustomerTypeDao customerTypeDao = new CustomerTypeDao();
				customerTypeList = customerTypeDao.getCustomerTypeList();
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Không có quyền truy cập", ""));
			}
		} else {
			detectionList = HistoryPeopleSignedUpDao.getPeopleSignedUp(null, null, null, null);
		}
	}
	
	public void searchDetectionList() {
		String strFromDate = null;
		String strToDate = null;
		try {
			strFromDate = Utils.convertDateToString(fromDate, "yyyy-MM-dd");
			strToDate = Utils.convertDateToString(toDate, "yyyy-MM-dd");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		detectionList = HistoryPeopleSignedUpDao.getPeopleSignedUp(peopleIdSearch, statusSearch, strFromDate, strToDate);
	}
	
	public String getPeopleSignedUp(Detection detection) {
		GroupsDao groupsDao = new GroupsDao();
		zone = groupsDao.getGroupList();
		CustomerTypeDao customerTypeDao = new CustomerTypeDao();
		customerTypeList = customerTypeDao.getCustomerTypeList();
		zoneSelected = detection.getGroupId();
		customerTypeSelected = detection.getCustomerTypeId();
		
		if (RoleConstant.ADMIN.equals(User.isUserInRoles())) {
			this.detection = detection;
			return "updatePeopleSignedUp";
		} else {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Không có quyền truy cập", ""));
		}
		return null;
	}

	public String updatePeople(People people) throws Exception {
		if (zoneSelected == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng chọn khu vực", ""));
			return "";
		}
		
		try {
			people.setImagePath(this.detection.getCapturedImagePath());
			people.setCustomerType(this.detection.getCustomerType());
			people.setDateOfBirth(this.detection.getDateOfBirth());
			people.setBirthday(this.detection.getBirthday());
			people.setFullName(this.detection.getFullName());
			people.setGender(this.detection.getGender());
			people.setPeopleId(this.detection.getPeopleId());
			people.setLastCheckinTime(this.detection.getCreatedTime());
			people.setMobilePhone(this.detection.getMobilePhone());
			people.setCustomerTypeId(customerTypeSelected);
			people.setGroupId(zoneSelected);
			
			return HistoryPeopleSignedUpDao.updatePeople(people, detection);
		} catch (Exception ex) {
			log.error("Cập nhất thất bại");
		}
		return null;
	}

	public Detection getDetection() {
		if (this.detection == null)
			this.detection = new Detection();
		return detection;
	}

	public void setDetection(Detection detection) {
		this.detection = detection;
	}
	
	public int getZoneSelected() {
		return zoneSelected;
	}

	public void setZoneSelected(int zoneSelected) {
		this.zoneSelected = zoneSelected;
	}

	public int getCustomerTypeSelected() {
		return customerTypeSelected;
	}

	public void setCustomerTypeSelected(int customerTypeSelected) {
		this.customerTypeSelected = customerTypeSelected;
	}

	public List<Groups> getZone() {
		return zone;
	}

	public void setZone(List<Groups> zone) {
		this.zone = zone;
	}

	public List<CustomerType> getCustomerTypeList() {
		return customerTypeList;
	}

	public void setCustomerTypeList(List<CustomerType> customerTypeList) {
		this.customerTypeList = customerTypeList;
	}

	public List<Detection> getDetectionList() {
		return detectionList;
	}

	public void setDetectionList(List<Detection> detectionList) {
		this.detectionList = detectionList;
	}

	public String getPeopleIdSearch() {
		return peopleIdSearch;
	}

	public void setPeopleIdSearch(String peopleIdSearch) {
		this.peopleIdSearch = peopleIdSearch;
	}

	public String getStatusSearch() {
		return statusSearch;
	}

	public void setStatusSearch(String statusSearch) {
		this.statusSearch = statusSearch;
	}

	public Date getFromDate() {
		return fromDate;
	}

	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	public Date getToDate() {
		return toDate;
	}

	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}
}
