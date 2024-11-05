package com.lvt.khvip.controller;

import java.io.Serializable;
import java.sql.Connection;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import org.primefaces.PrimeFaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.RoleConstant;
import com.lvt.khvip.constant.StatusConstant;
import com.lvt.khvip.dao.ConnectDB;
import com.lvt.khvip.dao.CustomerTypeDao;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.HistoryCapturedImagesDao;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.entity.CapturedImages;
import com.lvt.khvip.entity.CustomerType;
import com.lvt.khvip.entity.Groups;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.User;
import com.lvt.khvip.response.DataResponseFromRegisterFaceSearch;
import com.lvt.khvip.response.DataResponseFromRemoveFacesSearch;
import com.lvt.khvip.util.HttpUtils;
import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utils;

@ManagedBean
@ViewScoped
public class HistoryCapturedImagesController implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(HistoryCapturedImagesController.class);

	private CapturedImages capturedImages;
	private People peopleSelected;
	private int zoneSelected;
	private int customerTypeSelected;
	private List<Groups> zone;
	private List<CustomerType> customerTypeList;
	private List<People> peopleList;
	private boolean formUpdate = true;
	private String title;
	private List<CapturedImages> capturedImagesList;
	private String peopleIdSearch;
	private String statusSearch;
	private Date fromDate;
    private Date toDate;

	HttpUtils httpUtils = new HttpUtils();

	public HistoryCapturedImagesController() {
		Map<String, String> params =FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		String capturedImageId = params.get("capturedImageId");
		if(!StringUtils.isEmpty(capturedImageId)) {
			capturedImages = HistoryCapturedImagesDao.getCapturedImagesById(Integer.parseInt(capturedImageId));
			zoneSelected = capturedImages.getGroupId();
			customerTypeSelected = capturedImages.getCustomerTypeId();

			if (RoleConstant.ADMIN.equals(User.isUserInRoles())) {
				GroupsDao groupsDao = new GroupsDao();
				zone = groupsDao.getGroupList();
				CustomerTypeDao customerTypeDao = new CustomerTypeDao();
				customerTypeList = customerTypeDao.getCustomerTypeList();

				if (capturedImages.getStatus() == 1) {
					formUpdate = true;
					title = "Cập nhật";
				} else {
					formUpdate = false;
					title = "Đăng ký";
					capturedImages.setPeopleId("");
				}
			} else {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Không có quyền truy cập", ""));
			}
		} else {
			capturedImagesList = HistoryCapturedImagesDao.getPeopleCapturedImages(null, null, null, null);
		}
	}

	public void searchCapturedImagesList() {
		String strFromDate = null;
		String strToDate = null;
		try {
			strFromDate = Utils.convertDateToString(fromDate, "yyyy-MM-dd");
			strToDate = Utils.convertDateToString(toDate, "yyyy-MM-dd");
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		capturedImagesList = HistoryCapturedImagesDao.getPeopleCapturedImages(peopleIdSearch, statusSearch, strFromDate, strToDate);
	}

	public String action(People people, CapturedImages capturedImages) throws Exception {
		if (zoneSelected == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng chọn khu vực", ""));
			return "";
		}

		if (formUpdate) {
			return updatePeople(people, capturedImages);
		} else {
			return savePeople(people, capturedImages);
		}
	}

	public String updatePeople(People people, CapturedImages capturedImages) throws ParseException {
		try {
			people.setImagePath(this.capturedImages.getCapturedImagePath());
			people.setCustomerType(this.capturedImages.getCustomerType());
			people.setDateOfBirth(this.capturedImages.getDateOfBirth());
			people.setBirthday(this.capturedImages.getBirthday());
			people.setFullName(this.capturedImages.getFullName());
			people.setGender(this.capturedImages.getGender());
			people.setPeopleId(this.capturedImages.getPeopleId());
			people.setLastCheckinTime(this.capturedImages.getCreatedTime());
			people.setMobilePhone(this.capturedImages.getMobilePhone());
			people.setCustomerTypeId(customerTypeSelected);
			people.setGroupId(zoneSelected);
			capturedImages.setId(this.capturedImages.getId());

			return HistoryCapturedImagesDao.updatePeople(people, this.capturedImages);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cập nhật nhân viên thất bại", ""));
		}
		return null;
	}

	public String savePeople(People people, CapturedImages capturedImages) throws ParseException {
		if (PeopleDao.isExistPeople(this.capturedImages.getPeopleId())) {
			log.info("Nhân viên đã được đăng ký");
			showDialog("confirmReg");
			return "";
		}

		try {
			people.setImagePath(this.capturedImages.getCapturedImagePath());
			people.setCustomerType(this.capturedImages.getCustomerType());
			people.setDateOfBirth(this.capturedImages.getDateOfBirth());
			people.setBirthday(this.capturedImages.getBirthday());
			people.setFullName(this.capturedImages.getFullName());
			people.setGender(this.capturedImages.getGender());
			people.setPeopleId(this.capturedImages.getPeopleId());
			people.setLastCheckinTime(this.capturedImages.getCreatedTime());
			people.setMobilePhone(this.capturedImages.getMobilePhone());
			people.setCustomerTypeId(customerTypeSelected);
			people.setGroupId(zoneSelected);
			capturedImages.setId(this.capturedImages.getId());

			return HistoryCapturedImagesDao.saveNewPeople(people, this.capturedImages);
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Đăng ký nhân viên thất bại", ""));
		}
		return null;
	}

	public void reRegister () throws Exception {
		People people = new People();
		people.setFullName(capturedImages.getFullName());
		people.setDateOfBirth(capturedImages.getDateOfBirth());
		people.setBirthday(capturedImages.getBirthday());
		people.setCustomerTypeId(customerTypeSelected);
		people.setGroupId(zoneSelected);
		people.setImagePath(capturedImages.getCapturedImagePath());
		people.setMobilePhone(capturedImages.getMobilePhone());
		people.setGender(capturedImages.getGender());
		people.setPeopleId(capturedImages.getPeopleId());

		// TODO search face và đánh giá kéte quả

		Connection connection = ConnectDB.getConnection();
		connection.setAutoCommit(false);

		// insert db
		boolean result = HistoryCapturedImagesDao.reRegisterPeople(people, capturedImages, connection);
		if (result) {
			// gọi api xoá khuôn mặt
			DataResponseFromRemoveFacesSearch dataRemoveResponse = httpUtils.callAPIToRemove(capturedImages.getPeopleId());
			if (StatusConstant.STATUS_SUCCESS.equals(dataRemoveResponse.getStatus())
					|| StatusConstant.STATUS_PEOPLE_NOT_FOUND_ERROR.equals(dataRemoveResponse.getStatus())
					|| StatusConstant.STATUS_PEOPLE_DO_NOT_EXITS_ERROR.equals(dataRemoveResponse.getStatus())) {
				// gọi api đăng ký khuôn mặt
				DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreate(capturedImages.getCapturedImagePath(), capturedImages.getPeopleId());
				if (StatusConstant.STATUS_SUCCESS.equals(dataResponse.getStatus())) {
					connection.commit();
					log.info("Đăng ký lại SUCCESS");
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Đăng ký lại thành công nhân viên " + capturedImages.getFullName(), ""));
				} else {
					connection.rollback();
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Đăng ký lại nhân viên thất bại", ""));
				}
			} else {
				connection.rollback();
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Đăng ký lại nhân viên thất bại", ""));
			}
		} else {
			connection.rollback();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Đăng ký lại nhân viên thất bại", ""));
		}
		hideDialog("confirmReg");
	}

	public void onRowSelect() {
		capturedImages.setPeopleId(peopleSelected.getPeopleId());
		capturedImages.setFullName(peopleSelected.getFullName());
		capturedImages.setMobilePhone(peopleSelected.getMobilePhone());
		capturedImages.setDateOfBirth(peopleSelected.getDateOfBirth());
		capturedImages.setGender(peopleSelected.getGender());
		zoneSelected = peopleSelected.getGroupId();
	    customerTypeSelected = peopleSelected.getCustomerTypeId();
    }

	public void getPeopleListByPeopleId() {
		if (StringUtils.isEmpty(capturedImages.getPeopleId())) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng nhập mã nhân viên", ""));
			return;
		}
		peopleList = PeopleDao.searchPeopleList(capturedImages.getPeopleId(),null, null);
		showDialog("peoplePicker");
	}

	public void getPeopleListByFullName() {
		if (StringUtils.isEmpty(capturedImages.getFullName())) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng nhập số tên", ""));
			return;
		}
		peopleList = PeopleDao.searchPeopleList(null, capturedImages.getFullName(), null);
		showDialog("peoplePicker");
	}

	public void getPeopleListByMobilePhone() {
		if (StringUtils.isEmpty(capturedImages.getMobilePhone())) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng nhập số điện thoại", ""));
			return;
		}
		peopleList = PeopleDao.searchPeopleList(null, null, capturedImages.getMobilePhone());
		showDialog("peoplePicker");
	}

	public void showDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').show();");
	}

	public void hideDialog(String id) {
		PrimeFaces.current().executeScript("PF('" + id + "').hide();");
	}

	public CapturedImages getCapturedImages() {
		if (this.capturedImages == null)
			this.capturedImages = new CapturedImages();
		return capturedImages;
	}

	public void setCapturedImages(CapturedImages capturedImages) {
		this.capturedImages = capturedImages;
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

	public People getPeopleSelected() {
		return peopleSelected;
	}

	public void setPeopleSelected(People peopleSelected) {
		this.peopleSelected = peopleSelected;
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

	public List<People> getPeopleList() {
		return peopleList;
	}

	public void setPeopleList(List<People> peopleList) {
		this.peopleList = peopleList;
	}

	public boolean isFormUpdate() {
		return formUpdate;
	}

	public void setFormUpdate(boolean formUpdate) {
		this.formUpdate = formUpdate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<CapturedImages> getCapturedImagesList() {
		return capturedImagesList;
	}

	public void setCapturedImagesList(List<CapturedImages> capturedImagesList) {
		this.capturedImagesList = capturedImagesList;
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
