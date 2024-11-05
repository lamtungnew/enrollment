package com.lvt.khvip.controller;

import java.io.InputStream;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.file.UploadedFile;

import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.constant.StatusConstant;
import com.lvt.khvip.dao.ConnectDB;
import com.lvt.khvip.dao.CustomerTypeDao;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.entity.CustomerType;
import com.lvt.khvip.entity.Groups;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.User;
import com.lvt.khvip.response.DataResponseFromCheckFacesSearch;
import com.lvt.khvip.response.DataResponseFromRegisterFaceSearch;
import com.lvt.khvip.util.HttpUtils;
import com.lvt.khvip.util.MinioUtil;
import com.lvt.khvip.util.Utility;
import com.lvt.khvip.util.Utils;

@ManagedBean
@SessionScoped
public class RegisterFaceController {
	
	private String code;
	private String fullName;
	private String mobilePhone;
	private String dateOfBirth;
	private LocalDate birthday;
	private String gender;
	private int zoneSelected;
	private int customerTypeSelected;
	private boolean flagIsLiveless = true;
	private People peopleSelected;
	private List<Groups> zone;
	private List<CustomerType> customerTypeList;
	private List<People> peopleList;

	private String face;
	private List<List<String>> faces = new ArrayList<>();
	private UploadedFile fileUpload;
	private int	tabIndex = 1;

	private HttpUtils httpUtils = new HttpUtils();

	public RegisterFaceController() {
		GroupsDao groupsDao = new GroupsDao();
		zone = groupsDao.getGroupList();

		CustomerTypeDao customerTypeDao = new CustomerTypeDao();
		customerTypeList = customerTypeDao.getCustomerTypeList();
	}

	public void register() throws Exception {
		// tab đăng ký khuôn mặt
		if (tabIndex == 0) {
			String imagePath = Utils.convertByte64ToFile(faces.get(0).get(0), Constants.IMAGE_PATH);
			People people = new People();
			people.setPeopleId(code);
			people.setFullName(fullName);
			people.setDateOfBirth(birthday != null ? birthday.toString() : "");
			people.setGroupId(zoneSelected);
			people.setImagePath(imagePath);
			people.setMobilePhone(mobilePhone);
			people.setGender(gender);
			people.setCustomerTypeId(customerTypeSelected);

			Connection connection = ConnectDB.getConnection();
			connection.setAutoCommit(false);
			try {
				boolean result = false;
				
				if (!flagIsLiveless) {
					result = PeopleDao.registerPeople(people, connection)
							&& PeopleDao.registerPeopleRegImage(code, faces, connection)
							&& PeopleDao.registerPeopleWhitelist(code, User.getSessionUser().getId(), connection);
				} else {
					result = PeopleDao.registerPeople(people, connection)
							&& PeopleDao.registerPeopleRegImage(code, faces, connection);
				}

				if (result) {
//					// lưu file lên server
//					InputStream inputStream = fileUpload.getInputstream();
//					byte[] bytes = IOUtils.toByteArray(inputStream);
//					MinioUtil.uploadImage(fileName, bytes);
//					
//					String base64 = Base64.getEncoder().encodeToString(bytes);
//					// gọi api đăng ký khuôn mặt
//					DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToRegister(faces.get(0).get(0), code);
//					if (dataResponse != null && dataResponse.getStatus().equals(StatusConstant.STATUS_SUCCESS)) {
//						connection.commit();
//						showInfoMessage("Đăng ký thành công");
//						return;
//					} else {
//						connection.rollback();
//						showErrorMessage("Kiểm tra khuôn mặt thất bại");
//						return;
//					}
				} else {
					showErrorMessage("Đăng ký thất bại");
					return;
				}
			} catch (Exception e) {
				e.printStackTrace();
				connection.rollback();
			} finally {
				Utility.closeObject(connection);
				hideDialog("confirmReg");
			}
		}
		
		// tab đăng ký bằng ảnh tải lên
		else if (tabIndex == 1) {
			// lấy tên ảnh theo chuẩn
			String fileName = Utils.getNameFileImage(code);
			
			People people = new People();
			people.setPeopleId(code);
			people.setFullName(fullName);
			people.setDateOfBirth(birthday != null ? birthday.toString() : "");
			people.setGroupId(zoneSelected);
			people.setImagePath(fileName);
			people.setMobilePhone(mobilePhone);
			people.setGender(gender);
			people.setCustomerTypeId(customerTypeSelected);
			
			// TODO search face và đánh giá kéte quả
			// lưu file lên server
			InputStream inputStream = fileUpload.getInputStream();
			byte[] bytes = IOUtils.toByteArray(inputStream);
			MinioUtil.uploadImage(fileName, bytes);
			
			String base64 = Base64.getEncoder().encodeToString(bytes);
			
			DataResponseFromCheckFacesSearch checkFacesSearch = httpUtils.callAPIToSearch(Constants.SOURCE,
					base64);
			
			String peopleId = checkFacesSearch.getPeopleIdOfFace();
			if (peopleId != null) {
				showErrorMessage("Khuôn mặt đã tồn tại với ID : " + peopleId);
				return; 
			}

			Connection connection = ConnectDB.getConnection();
			connection.setAutoCommit(false);
			try {
				boolean result = PeopleDao.registerPeople(people, connection) && PeopleDao.registerPeopleRegImage(code, fileName, connection);
				
				if (result) {
					// lưu file lên server
//					InputStream inputStream = fileUpload.getInputStream();
//					byte[] bytes = IOUtils.toByteArray(inputStream);
//					MinioUtil.uploadImage(fileName, bytes);
//					
//					String base64 = Base64.getEncoder().encodeToString(bytes);
					// gọi api đăng ký khuôn mặt
					DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToRegister(base64, code);
					if (dataResponse != null && dataResponse.getStatus().equals(StatusConstant.STATUS_SUCCESS)) {
						connection.commit();
						showInfoMessage("Đăng ký thành công");
						clear();
						return;
					} else {
						connection.rollback();
						showErrorMessage("Kiểm tra khuôn mặt thất bại");
						return;
					}
				} else {
					showErrorMessage("Đăng ký thất bại");
					return;
				}
			} catch (Exception e) {
				connection.rollback();
				showErrorMessage("Đăng ký thất bại");
			} finally {
				Utility.closeObject(connection);
				hideDialog("confirmReg");
			}
		}
		
	}
	
	public void validate() throws Exception {
		// valid input
		if (StringUtils.isBlank(code)) {
			showWarnMessage("Trường 'Mã nhân viên' bắt buộc phải nhập");
			return;
		}
		if (StringUtils.isBlank(fullName)) {
			showWarnMessage("Trường 'Họ và tên' bắt buộc phải nhập");
			return;
		}
		if (StringUtils.isBlank(mobilePhone)) {
			showWarnMessage("Trường 'Số điện thoại' bắt buộc phải nhập");
			return;
		}
		if (zoneSelected == 0) {
			showWarnMessage("Vui lòng chọn khu vực");
			return;
		}

		if (PeopleDao.isExistPeople(code)) {
			PeopleDao.updateExistPeople(code);
			showErrorMessage("Mã nhân viên đã được đăng ký");
			return;
		}
		
		// tab đăng ký khuôn mặt
		if (tabIndex == 0) {
			if (faces == null || faces.size() <= 0) {
				showWarnMessage("Vui lòng chụp ảnh khuôn mặt. Được phép chụp tối đa 5 khuôn mặt");
				return;
			}
			
			// check khuôn mặt
			for (List<String> list : faces) {
				for (String imageBase64 : list) {
					flagIsLiveless = httpUtils.checkLiveless(imageBase64);
					if (!flagIsLiveless) {
						break;
					}
				}
				if (!flagIsLiveless) {
					break;
				}
			}
			
			if (!flagIsLiveless) {
				showDialog("confirmReg");
			} else {
				register();
			}
			
		// tab đăng ký bằng ảnh tải lên
		} else if (tabIndex == 1) {
			if (fileUpload == null) {
				showWarnMessage("Vui lòng tải ảnh lên");
				return;
			}
			
			register();
		}
	}
	
	public void addFace() {
		if (StringUtils.isNotBlank(face)) {
			if (faces.size() < 5) {
				faces.add(Arrays.asList(StringUtils.split(face, ";")));
			}
		}
	}
	
	public void onRowSelect() {
		code = peopleSelected.getPeopleId();
		fullName = peopleSelected.getFullName();
		mobilePhone = peopleSelected.getMobilePhone();
		dateOfBirth = peopleSelected.getDateOfBirth();
		gender = peopleSelected.getGender();
		zoneSelected = peopleSelected.getGroupId();
	    customerTypeSelected = peopleSelected.getCustomerTypeId();
    }
	
	public void getPeopleListByPeopleId() {
		if (StringUtils.isEmpty(code)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", "Vui lòng nhập mã nhân viên"));
			return;
		}
		peopleList = PeopleDao.searchPeopleList(code, null, null);	
		showDialog("peoplePicker");
	}
	
	public void getPeopleListByFullName() {
		if (StringUtils.isEmpty(fullName)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", "Vui lòng nhập mã tên"));
			return;
		}
		peopleList = PeopleDao.searchPeopleList(null, fullName, null);	
		showDialog("peoplePicker");
	}
	
	public void getPeopleListByMobilePhone() {
		if (StringUtils.isEmpty(mobilePhone)) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", "Vui lòng nhập số điện thoại"));
			return;
		}
		peopleList = PeopleDao.searchPeopleList(null, null, mobilePhone);	
		showDialog("peoplePicker");
	}
	
	public void clear() {
		code = null;
		fullName = null;
		mobilePhone = null;
		dateOfBirth = null;
		birthday = null;
		gender = null;
		zoneSelected = 0;
		customerTypeSelected = 0;
		peopleSelected = null;
		face = null;
		faces = null;
	}

	public void removeFace(String index) {
		faces.remove(Integer.valueOf(index).intValue());
	}

	public void showInfoMessage(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Thông báo: ", message));
	}
	
	public void showWarnMessage(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Cảnh báo", message));
	}

	public void showErrorMessage(String message) {
		FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi: ", message));
	}

	public void showDialog(String id) {
//		RequestContext.getCurrentInstance().execute("PF('" + id + "').show();");
		PrimeFaces.current().executeScript("PF('" + id + "').show();");
	}

	public void hideDialog(String id) {
//		RequestContext.getCurrentInstance().execute("PF('" + id + "').hide();");
		PrimeFaces.current().executeScript("PF('" + id + "').hide();");
	}
	
	public void onTabChange(TabChangeEvent event) {
	    tabIndex = ((TabView) event.getSource()).getIndex();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	public String getFace() {
		return face;
	}

	public void setFace(String face) {
		this.face = face;
	}

	public List<List<String>> getFaces() {
		return faces;
	}

	public void setFaces(List<List<String>> faces) {
		this.faces = faces;
	}

	public List<Groups> getZone() {
		return zone;
	}

	public void setZone(List<Groups> zone) {
		this.zone = zone;
	}

	public int getZoneSelected() {
		return zoneSelected;
	}

	public void setZoneSelected(int zoneSelected) {
		this.zoneSelected = zoneSelected;
	}

	public List<CustomerType> getCustomerTypeList() {
		return customerTypeList;
	}

	public void setCustomerTypeList(List<CustomerType> customerTypeList) {
		this.customerTypeList = customerTypeList;
	}

	public int getCustomerTypeSelected() {
		return customerTypeSelected;
	}

	public void setCustomerTypeSelected(int customerTypeSelected) {
		this.customerTypeSelected = customerTypeSelected;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public boolean isFlagIsLiveless() {
		return flagIsLiveless;
	}

	public void setFlagIsLiveless(boolean flagIsLiveless) {
		this.flagIsLiveless = flagIsLiveless;
	}

	public UploadedFile getFileUpload() {
		return fileUpload;
	}

	public void setFileUpload(UploadedFile fileUpload) {
		this.fileUpload = fileUpload;
	}

	public int getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}

	public People getPeopleSelected() {
		return peopleSelected;
	}

	public void setPeopleSelected(People peopleSelected) {
		this.peopleSelected = peopleSelected;
	}

	public List<People> getPeopleList() {
		return peopleList;
	}

	public void setPeopleList(List<People> peopleList) {
		this.peopleList = peopleList;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}
	
	
	
}
