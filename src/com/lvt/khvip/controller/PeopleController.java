/**
 *
 */
package com.lvt.khvip.controller;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.RoleConstant;
import com.lvt.khvip.dao.CustomerTypeDao;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.entity.CustomerType;
import com.lvt.khvip.entity.Groups;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.User;

/**
 * @author: tuha.lvt
 *
 * @date:
 */
@ManagedBean
@SessionScoped
public class PeopleController implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(PeopleController.class);

	private String peopleIdSearch;
	private String fullNameSearch;
	private String mobilePhoneSearch;
	private int zoneSelected;
	private int customerTypeSelected;
	private List<Groups> zone;
	private List<CustomerType> customerTypeList;
	private List<People> peopleList;
	private List<String> imageList;
    private People people;
    private UploadedFile imageFile;
    private String originalFaceImage;
    
    public PeopleController() {
    	peopleList = PeopleDao.getListPeople();
    	GroupsDao groupsDao = new GroupsDao();
		zone = groupsDao.getGroupList();
		CustomerTypeDao customerTypeDao = new CustomerTypeDao();
		customerTypeList = customerTypeDao.getCustomerTypeList();
	}

	public String getPeople(People people) {
		imageList = PeopleDao.getLast5Detection(people.getPeopleId());
		zoneSelected = people.getGroupId();
		customerTypeSelected = people.getCustomerTypeId();
    	
        if (RoleConstant.ADMIN.equals(User.isUserInRoles())) {
            this.people = people;
            this.originalFaceImage = people.getImagePath();
            return "updatePeople";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Không có quyền truy cập", ""));
        }
        return null;
    }
    
    public void searchPeople() {
    	peopleList = PeopleDao.searchPeopleList(peopleIdSearch, fullNameSearch, mobilePhoneSearch);
    }

	public void updatePeople() {
		if (zoneSelected == 0) {
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Vui lòng chọn khu vực", ""));
			return;
		}
		try {
			boolean regFace = false;
			if (people.getImagePath() != null && !people.getImagePath().equals(originalFaceImage)) {
				regFace = true;
			}
			
			if (regFace) {
				log.info("[PeopleID={}] Updating face. Old={}. New={}", people.getPeopleId(), originalFaceImage, people.getImagePath());
			}
			PeopleDao.updatePeople(people, regFace);
			originalFaceImage = people.getImagePath();
			FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Cập nhật thông tin nhân viên thành công", ""));
		} catch (Exception ex) {
			log.error(ex.getMessage(), ex);
		}
	}
//    public String updatePeople(People people) {
//    	if (imageFile == null) {
//    		try {
//    			DataResponseFromRemoveFacesSearch dataRemoveResponse = httpUtils.callAPIToRemove(idDeleted);
//    			if (dataRemoveResponse.getStatus().equals(StatusConstant.STATUS_SUCCESS)) {
//    				HistoryCapturedImagesDao.deletePeople(idDeleted);
//    			} else {
//    				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
//    						"Thất bại", "Cập nhật thất bại"));
//    			}
//    			DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreate(this.people.getImagePath(), this.people.getLastCheckinTime(), this.people.getPeopleId());
//    			if (dataResponse.getStatus().equals(StatusConstant.STATUS_SUCCESS)) {
//    				people.setImagePath(this.people.getImagePath());
//    				people.setCustomerType(this.people.getCustomerType());
//    				people.setDateOfBirth(this.people.getDateOfBirth());
//    				people.setFullName(this.people.getFullName());
//    				people.setGender(this.people.getGender());
//    				people.setPeopleId(this.people.getPeopleId());
//    				return PeopleDao.saveNewPeople(people);
//    			}
//    		} catch (Exception ex) {
//    			log.error("Thêm thất bại");
//    		}
//    	} else {
//    		try {
//    			PeopleFile peopleFile = new PeopleFile();
//    			DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreateHaveFile(imageFile, this.people.getPeopleId());
//    			if (dataResponse.getStatus().equals(StatusConstant.STATUS_SUCCESS)) {
//    				
//    				String filePath = peopleFile.getUploadFilePath(imageFile.getFileName());
//    				peopleFile.copyFile(filePath, imageFile);
//    				Path path = Paths.get(filePath);
//    				if (!Files.exists(path)) {
//    					Files.createFile(path);
//    				}
//    				Set<PosixFilePermission> perms = Files.readAttributes(path, PosixFileAttributes.class)
//    						.permissions();
//    				
//    				perms.add(PosixFilePermission.OWNER_WRITE);
//    				perms.add(PosixFilePermission.OWNER_READ);
//    				perms.add(PosixFilePermission.GROUP_WRITE);
//    				perms.add(PosixFilePermission.GROUP_READ);
//    				perms.add(PosixFilePermission.OTHERS_READ);
//    				Files.setPosixFilePermissions(path, perms);
//    				
//    				log.info("file " + imageFile.getFileName());
//    				people.setCustomerType(this.people.getCustomerType());
//    				people.setDateOfBirth(this.people.getDateOfBirth());
//    				people.setFullName(this.people.getFullName());
//    				people.setGender(this.people.getGender());
//    				people.setPeopleId(this.people.getPeopleId());
//    				people.setImagePath(filePath);
//    				String url = PeopleDao.saveNewPeople(people);
//    				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
//    						"Thành công", "Đã thêm" + people.getFullName()));
//    				FacesContext.getCurrentInstance().getExternalContext().redirect(url);
//    			} else {
//    				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
//    						"Thất bại", "Ảnh không hợp lệ. Vui lòng chọn ảnh khác"));
//    			}
//    		} catch (Exception ex) {
//    			ex.printStackTrace();
//    			FacesContext.getCurrentInstance().addMessage(null,
//    					new FacesMessage(FacesMessage.SEVERITY_WARN, "Thất bại", "Trùng mã cá nhân"));
//    		}
//    	}
//    	return null;
//    }
	
	public void onRowSelect(SelectEvent<String> event) {
        people.setImagePath(event.getObject());
    }

    public UploadedFile getImageFile() {
        return imageFile;
    }

    public void setImageFile(UploadedFile imageFile) {
        this.imageFile = imageFile;
    }

    public People getPeople() {
        if (this.people == null)
            this.people = new People();
        return people;
    }

    public void setPeople(People people) {
        this.people = people;
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

	public List<People> getPeopleList() {
		return peopleList;
	}

	public void setPeopleList(List<People> peopleList) {
		this.peopleList = peopleList;
	}

	public String getPeopleIdSearch() {
		return peopleIdSearch;
	}

	public void setPeopleIdSearch(String peopleIdSearch) {
		this.peopleIdSearch = peopleIdSearch;
	}

	public String getFullNameSearch() {
		return fullNameSearch;
	}

	public void setFullNameSearch(String fullNameSearch) {
		this.fullNameSearch = fullNameSearch;
	}

	public String getMobilePhoneSearch() {
		return mobilePhoneSearch;
	}

	public void setMobilePhoneSearch(String mobilePhoneSearch) {
		this.mobilePhoneSearch = mobilePhoneSearch;
	}

	public List<String> getImageList() {
		return imageList;
	}

	public void setImageList(List<String> imageList) {
		this.imageList = imageList;
	}
	
}
