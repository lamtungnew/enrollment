package com.lvt.khvip.entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

import org.primefaces.event.SelectEvent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.constant.RoleConstant;
import com.lvt.khvip.constant.StatusConstant;
import com.lvt.khvip.dao.CustomerTypeDao;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.response.DataResponseFromRegisterFaceSearch;
import com.lvt.khvip.util.HttpUtils;

@ManagedBean
@RequestScoped
public class PeopleFile {
	private static final Logger log = LoggerFactory.getLogger(PeopleFile.class);

	private int id;
	private String peopleId;
	private String fullName;
	private String dateOfBirth;
	private String gender;
	private String customerType;
	private String lastCheckinTime;
	private String mobilePhone;
	private int zoneSelected;
	private int customerTypeSelected;
	private String peopleSelected;
	private List<Groups> zone;
	private List<CustomerType> customerTypeList;
	private List<People> peopleList;
	
	private UploadedFile imagePath;
	
	HttpUtils httpUtils = new HttpUtils();
	
	public PeopleFile() {
		GroupsDao groupsDao = new GroupsDao();
		zone = groupsDao.getGroupList();

		CustomerTypeDao customerTypeDao = new CustomerTypeDao();
		customerTypeList = customerTypeDao.getCustomerTypeList();
		
		peopleList = PeopleDao.getPeopleList();
	}

	public void upload(PeopleFile peopleFile, People people) {

		if (RoleConstant.ADMIN.equals(User.isUserInRoles())) {
			if (PeopleDao.isExistPeople(peopleSelected)) {
				FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Thất bại", "Mã nhân viên đã được đăng ký"));
			}
			
			try {
				DataResponseFromRegisterFaceSearch dataResponse = httpUtils.callAPIToCreateHaveFile(peopleFile.getImagePath(), peopleFile.getPeopleId());
				if (dataResponse.getStatus().equals(StatusConstant.STATUS_SUCCESS)) {

					String filePath = getUploadFilePath(imagePath.getFileName());
					copyFile(filePath, imagePath);
					Path path = Paths.get(filePath);
					if (!Files.exists(path)) {
						Files.createFile(path);
					}
//					Set<PosixFilePermission> perms = Files.readAttributes(path, PosixFileAttributes.class).permissions();
//
//					perms.add(PosixFilePermission.OWNER_WRITE);
//					perms.add(PosixFilePermission.OWNER_READ);
//					perms.add(PosixFilePermission.GROUP_WRITE);
//					perms.add(PosixFilePermission.GROUP_READ);
//					perms.add(PosixFilePermission.OTHERS_READ);
//					Files.setPosixFilePermissions(path, perms);

					log.info("file " + imagePath.getFileName());
					people.setCustomerType(peopleFile.getCustomerType());
					people.setDateOfBirth(peopleFile.getDateOfBirth());
					people.setFullName(peopleFile.getFullName());
					people.setGender(peopleFile.getGender());
//					people.setPeopleId(peopleFile.getPeopleId());
					people.setImagePath(filePath);
//					String url = PeopleDao.saveNewPeople(people);
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Thành công", "Đã thêm" + people.getFullName()));
//					FacesContext.getCurrentInstance().getExternalContext().redirect(url);
				} else {
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
							"Thất bại", "Ảnh không hợp lệ. Vui lòng chọn ảnh khác"));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				FacesContext.getCurrentInstance().addMessage(null,
						new FacesMessage(FacesMessage.SEVERITY_WARN, "Thất bại", "Trùng mã cá nhân"));
			}
		} else {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_WARN, "Không có quyền truy cập", ""));
		}
	}

	public void copyFile(String filePath, UploadedFile uploadedFile) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = uploadedFile.getInputStream();
			// write the inputStream to a FileOutputStream
			File file = new File(filePath);
			log.info(file.getAbsolutePath());
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			log.info(file.getAbsolutePath());
			out = new FileOutputStream(file);
			int read = 0;
			byte[] bytes = new byte[1024 * 1024];

			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			in.close();
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String getUploadFilePath(String fileName) {
		try {
			String afterFilePath = Constants.IMAGE_PATH + "face" + fileName;
			return afterFilePath;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}

	}
	
	public void onItemSelectedListener(SelectEvent<String> event){
	    String selectedItem = event.getObject();
	    People itemP = peopleList.stream()
	    		  .filter(item -> selectedItem.equals(item.getPeopleId()))
	    		  .findAny()
	    		  .orElse(null);
	    
	    zoneSelected = itemP.getGroupId();
	    fullName = itemP.getFullName();
	    mobilePhone = itemP.getMobilePhone();
	    dateOfBirth = itemP.getDateOfBirth();
	    gender = itemP.getGender();
	    customerTypeSelected = itemP.getCustomerTypeId();
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

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(String dateOfBirth) {
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

	public UploadedFile getImagePath() {
		return imagePath;
	}

	public void setImagePath(UploadedFile imagePath) {
		this.imagePath = imagePath;
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

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getPeopleSelected() {
		return peopleSelected;
	}

	public void setPeopleSelected(String peopleSelected) {
		this.peopleSelected = peopleSelected;
	}

	public List<People> getPeopleList() {
		return peopleList;
	}

	public void setPeopleList(List<People> peopleList) {
		this.peopleList = peopleList;
	}
	
}
