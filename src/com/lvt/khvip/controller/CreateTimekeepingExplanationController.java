/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.lvt.khvip.client.ApprovePeopleClient;
import com.lvt.khvip.client.TimeKeepingSheetClient;
import com.lvt.khvip.client.dto.TimekeepingSheetData;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.dao.TimekeepingDateDao;
import com.lvt.khvip.dao.TimekeepingExplanationDao;
import com.lvt.khvip.entity.*;
import com.lvt.khvip.model.TimeKeepingSheetDataModel;
import com.lvt.khvip.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Getter
@Setter
@ManagedBean
@SessionScoped
public class CreateTimekeepingExplanationController implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(CreateTimekeepingExplanationController.class);

	private List<People> listPeopleForSearch;
	private List<People> listSupervisor;
	private List<People> listApprovePeople;
	private ApprovePeople approvePeople;
	private Map<String,People> peopleByFullName = new HashMap();
	private Map<String,People> peopleByPeopleId = new HashMap();

	private List<TimekeepingSheetExcel> listTimekeepingSheet;
	private List<Groups> listGroupForSearch;

	private TimekeepingSheetData searchTimekeepingSheet = new TimekeepingSheetData();

	private GroupsDao groupsDao;
	private ExcelUtils excelUtils = new ExcelUtils();
	private UploadedFile uploadedExcel;
	private UploadedFile uploadedImage;
	private String uploadExcelStatus;
	private String errorSummary;
	private byte[] attachedErrorFileByteArr;
    private StreamedContent content ;
    private StreamedContent exportExcelContent ;
    private String title;
    private boolean disableErrorLink = true;
    private TimeKeepingSheetClient timeKeepingSheetClient = new TimeKeepingSheetClient();
	private ApprovePeopleClient approvePeopleClient = new ApprovePeopleClient();
	private TimeKeepingSheetDataModel dataModel;
    private Date fromDate;
    private Date toDate;
	private String rootPath = ConfProperties.getProperty("root.path");
	private String imageFolder = ConfProperties.getProperty("image.folder");
	private String imageRootUrl = ConfProperties.getProperty("image.root.url");
	private TimekeepingSheetData updateTimeKeepingSheet = new TimekeepingSheetData();
	private List<TimekeepingSheetData> listUpdateTimeKeepingSheet = new ArrayList();
	private List<TimekeepingDate> listTimekeepingDate = new ArrayList();
	private User user;
	private ApprovalFlow approvalFlow;
	private People userInfo;

    public void reset() {
    	uploadExcelStatus = "";
    	errorSummary = "";
    	content = null;
    	disableErrorLink = true;
    	title = "";
    	uploadedExcel = null;
    }

    public void initUser(){
    	user = (User) SessionUtils.getSession().getAttribute("user-info");
		userInfo = peopleByPeopleId.get(user.getPeopleId());
	}

    public void resetFilter() {
    	if (!ObjectUtils.isEmpty(searchTimekeepingSheet)) {
    		searchTimekeepingSheet.setCreatedAt(null);
    		searchTimekeepingSheet.setState(null);
        	initTblTimeKeepkingExplanation();
    	}
    }

    public void initListUpdateTimeKeepingSheet(String action) {
    	if (action.equals("ADD")){
    		TimekeepingSheetData timekeepingSheet = new TimekeepingSheetData();
    		listUpdateTimeKeepingSheet.add(timekeepingSheet);
		}

	}

    public void initTimeKeepingDate(){
		listTimekeepingDate = TimekeepingDateDao.getListTimeKeepingDate(null);
	}

    public CreateTimekeepingExplanationController() {
		TimeZone tz = Calendar.getInstance().getTimeZone();
		System.out.println(tz.getDisplayName()); // (i.e. Moscow Standard Time)
		System.out.println(tz.getID());

    	groupsDao = new GroupsDao();
		initListGroupForSearch();
		initListPeopleForSearch();
		initUser();
		initApprovalFlow();
    	initTblTimeKeepkingExplanation();
    	initTimeKeepingDate();
		initListApprovePeople();
	}

	public void initApprovalFlow(){
    	if (ObjectUtils.isEmpty(user)) {
    		return;
		}

    	approvalFlow = TimekeepingExplanationDao.getApprovalFlowByGroupId(userInfo.getGroupId(), Constants.ApprovalFLow.KEEPING_APPROVAL);

    	if (ObjectUtils.isEmpty(approvalFlow)){
    		return;
		}

		Integer groupId = 0;
		String role = "";

		if (approvalFlow.getApprovalLevel1().equals(Constants.RoleConstants.DEP_LEADER)){
			groupId = approvalFlow.getGroupId();
			role = Constants.RoleConstants.DEP_LEADER;
		} else if (approvalFlow.getApprovalLevel1().equals(Constants.RoleConstants.GROUP_LEADER)){
			groupId = approvalFlow.getDepId();
			role = Constants.RoleConstants.GROUP_LEADER;
		} else if (approvalFlow.getApprovalLevel1().equals(Constants.RoleConstants.ADMIN)){
			role = Constants.RoleConstants.ADMIN;
		}

		User approvalPeopleLevel1 = PeopleDao.getApprovalNameByGroupId(groupId, role);
		if (!ObjectUtils.isEmpty(approvalPeopleLevel1)) {
			approvalFlow.setApprovalLevel1PeopleId(approvalPeopleLevel1.getPeopleId());
			approvalFlow.setApprovalLevel1Name(approvalPeopleLevel1.getFullName());
		}

		if (approvalFlow.getApprovalLevel().equals(2)){
			Integer groupIdLevel2 = 0;
			String roleLevel2 = "";

			if (approvalFlow.getApprovalLevel2().equals(Constants.RoleConstants.DEP_LEADER)){
				groupIdLevel2 = approvalFlow.getGroupId();
				roleLevel2 = Constants.RoleConstants.DEP_LEADER;
			} else if (approvalFlow.getApprovalLevel2().equals(Constants.RoleConstants.GROUP_LEADER)){
				groupIdLevel2 = approvalFlow.getDepId();
				roleLevel2 = Constants.RoleConstants.GROUP_LEADER;
			} else if (approvalFlow.getApprovalLevel2().equals(Constants.RoleConstants.ADMIN)){
				roleLevel2 = Constants.RoleConstants.ADMIN;
			}

			User approvalPeopleLevel2 = PeopleDao.getApprovalNameByGroupId(groupIdLevel2,roleLevel2);

			if (!ObjectUtils.isEmpty(approvalPeopleLevel2)){
				approvalFlow.setApprovalLevel2PeopleId(approvalPeopleLevel2.getPeopleId());
				approvalFlow.setApprovalLevel2Name(approvalPeopleLevel2.getFullName());
			}
		}
	}

	public Integer getGroupIdByRole(String role){
    	Integer groupId = null;
		if (!StringUtils.isEmpty(role)
				&& role.equals(Constants.RoleConstants.DEP_LEADER)){
			groupId = userInfo.getGroupId();
		} else if (!StringUtils.isEmpty(role)
				&& role.equals(Constants.RoleConstants.GROUP_LEADER) && !ObjectUtils.isEmpty(listGroupForSearch)) {

			Map<Integer,Groups> groupById = listGroupForSearch.stream().collect(Collectors.toMap(groupItem -> groupItem.getGroupId(),Function.identity(),
					(existing, replacement) -> existing));
			Groups group = groupById.get(userInfo.getGroupId());

			groupId = group.getParentId();
		}
    	return groupId;
	}
	
	public void handleUploadImage(StringBuilder inputFilePath, StringBuilder imageUrl, UploadedFile uploadedImage) {
		if (ObjectUtils.isEmpty(uploadedImage)) {
			return;
		}
		
		boolean checkRealImage = checkRealImg(uploadedImage);
		if (!checkRealImage) {
			logMessage(FacesMessage.SEVERITY_ERROR, MessageProvider.getValue("upload.image.invalid.file"));
			return;
		}

		String extension = uploadedImage.getFileName().substring(uploadedImage.getFileName().lastIndexOf(".") + 1);
		String fileName = "uploadedImage_" + System.currentTimeMillis() + "." + extension;
		String filePath = rootPath +"/" + imageFolder +"/"+ fileName;
		File targetFile = new File(filePath);
		long imageSize = uploadedImage.getSize();
		if (imageSize > 1000000) {
			BufferedImage resizedImage = resizeImage(uploadedImage);
			try {
				ImageIO.write(resizedImage, extension, targetFile);
			} catch (Exception ex) {
				log.error("FAIL TO HANDLE FILE UPLOAD!", ex);
			}

		} else {
			try (OutputStream out = new FileOutputStream(targetFile); InputStream in = uploadedImage.getInputStream();) {
				ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
				URI uri = new URI(ext.getRequestScheme(), null, ext.getRequestServerName(), ext.getRequestServerPort(),
						ext.getRequestContextPath(), null, null);
				int read = 0;
				byte[] bytes = new byte[1024];

				while ((read = in.read(bytes)) != -1) {
					out.write(bytes, 0, read);
				}
				out.flush();
			} catch (Exception ex) {
				log.error("FAIL TO HANDLE FILE UPLOAD!", ex);
			}
		}
		inputFilePath.append(filePath);
		imageUrl.append(imageRootUrl + "/" + fileName);
	}
	
	private void logMessage(Severity severity, String text) {
		FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(severity, text, null));
	}
	
	private BufferedImage resizeImage(UploadedFile file) {
		BufferedImage resizedImage = null;
		try {
			BufferedImage oldImage = ImageIO.read(file.getInputStream());
			int newWidth = oldImage.getWidth() / 4 * 3;
			int newHeigh = oldImage.getHeight() / 4 * 3;

			int type = oldImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : oldImage.getType();

			resizedImage = new BufferedImage(newWidth, newHeigh, type);
			Graphics2D graphic = resizedImage.createGraphics();
			graphic.drawImage(oldImage, 0, 0, newWidth, newHeigh, null);
			graphic.dispose();
			graphic.setComposite(AlphaComposite.Src);

			// keep quality of image after resize
			graphic.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			graphic.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			graphic.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		} catch (IOException e) {
			e.printStackTrace();
		}
		return resizedImage;
	}
	
	public boolean checkRealImg(UploadedFile f) {
		try (ImageInputStream iis = ImageIO.createImageInputStream(f.getInputStream())) {

			Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
			if (!iter.hasNext()) {
				return false;
			}

		} catch (Exception e2) {
			e2.printStackTrace();
		}
		return true;
	}
	
	public void closeDlgUploadStatus() {
		reset();
	}

	public void closeDlgAddNewTimeKeepingSheet() {
		updateTimeKeepingSheet = new TimekeepingSheetData();
		listUpdateTimeKeepingSheet.clear();
	}
	
	public void downloadFile() { 
		try {			
			String fileName = ""
					+ "time_checking_error_"+new Date().getTime()+".xlsx";
			InputStream downloadFileInputStream = new ByteArrayInputStream(attachedErrorFileByteArr);
			DefaultStreamedContent content = DefaultStreamedContent.builder()
					.name(fileName)
					.stream(() -> downloadFileInputStream).build();
			this.content = content;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}	    
	}
	
	public void changeState(String newState) {
		TimekeepingSheetData updateItem = new TimekeepingSheetData();
		updateItem.setAutoid(updateTimeKeepingSheet.getAutoid());
		updateItem.setState(newState);
		timeKeepingSheetClient.updateTimeKeepingSheet(updateItem);
		initTblTimeKeepkingExplanation();
	}
	
	public void initSelectedTimeKeepingSheet(TimekeepingSheetData timeKeepingSheetData) {
		this.updateTimeKeepingSheet = timeKeepingSheetData;
		TimekeepingSheetData condition = new TimekeepingSheetData();
		condition.setAutoid(updateTimeKeepingSheet.getAutoid());
		TimekeepingSheetData timeKeepingSheet = timeKeepingSheetClient.getTimeKeepingSheet(condition);

		updateTimeKeepingSheet.setTimeKeepingDate(timeKeepingSheet.getTimeKeepingDate());
		updateTimeKeepingSheet.setSupervisor(timeKeepingSheet.getSupervisor());

		if (!ObjectUtils.isEmpty(updateTimeKeepingSheet.getTimeKeepingDate())){
			List<String> listTimeKeepingDate = new ArrayList<>();
			listTimeKeepingDate = updateTimeKeepingSheet.getTimeKeepingDate().stream()
					.map(timekeepingDate -> timekeepingDate.getDate()).collect(Collectors.toList());
			updateTimeKeepingSheet.setTimeKeepingDateCbxValues(listTimeKeepingDate.toArray(new String[0]));
		}

		listUpdateTimeKeepingSheet.clear();
		listUpdateTimeKeepingSheet.add(updateTimeKeepingSheet);
    }

    public void initListApprovePeople(){
    	if (!ObjectUtils.isEmpty(approvalFlow)){
    		approvePeople = approvePeopleClient.getApprovePeople(user.getPeopleId(),user.getUsername(),
					approvalFlow.getApprovalType(), 1);
			listApprovePeople = approvePeople.getPeopleIds();
		}
	}

    public void initAddNewTimeKeepingSheet(){
		if (ObjectUtils.isEmpty(user) || StringUtils.isEmpty(user.getPeopleId())){
    		return;
		}

		People people = peopleByPeopleId.get(user.getPeopleId());

		if (!ObjectUtils.isEmpty(people)){
			updateTimeKeepingSheet.setFullName(people.getFullName());
			updateTimeKeepingSheet.setPeopleId(people.getPeopleId());
			updateTimeKeepingSheet.setGroupId(people.getGroupId());
			updateTimeKeepingSheet.setGroupName(people.getGroupName());

			if (StringUtils.isEmpty(updateTimeKeepingSheet.getApprovedBy()) && !ObjectUtils.isEmpty(listApprovePeople)
					&& listApprovePeople.size() == 1) {
				updateTimeKeepingSheet.setApprovedBy(listApprovePeople.get(0).getPeopleId());
			}
		}
	}
	
    public void initTblTimeKeepkingExplanation() {
		Date convertedFromDate = null;
		if (!ObjectUtils.isEmpty(fromDate)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fromDate);
			calendar.add(Calendar.HOUR, 00);
			calendar.add(Calendar.MINUTE, 00);
			calendar.add(Calendar.SECOND, 00);
			convertedFromDate = calendar.getTime();
		}

		Date convertedToDate = null;
		if (!ObjectUtils.isEmpty(toDate)){
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(toDate);
			calendar.add(Calendar.HOUR, 23);
			calendar.add(Calendar.MINUTE, 59);
			calendar.add(Calendar.SECOND, 59);
			convertedToDate = calendar.getTime();
		}

		searchTimekeepingSheet.setKeepingDateFrom(convertedFromDate);
		searchTimekeepingSheet.setKeepingDateTo(convertedToDate);

		if (!ObjectUtils.isEmpty(user) && !StringUtils.isEmpty(user.getPeopleId())){
			searchTimekeepingSheet.setPeopleId(user.getPeopleId());
			dataModel = new TimeKeepingSheetDataModel("tblListApprove", searchTimekeepingSheet, timeKeepingSheetClient);
		}
    }

    public void initListPeopleForSearch() {
    	listPeopleForSearch = PeopleDao.getListPeople();
		peopleByFullName = listPeopleForSearch.stream().collect
				(Collectors.toMap(people -> people.getFullName(), Function.identity(), (p1, p2) ->{
					//todo
					return null;} ));

		peopleByPeopleId = listPeopleForSearch.stream().collect
				(Collectors.toMap(people -> people.getPeopleId(), Function.identity(), (p1, p2) ->{
					//todo
					return null;} ));
    }

    public void initListGroupForSearch() {
    	listGroupForSearch = groupsDao.getGroupList();
    }

    public void search() {
    	initTblTimeKeepkingExplanation();
    }

	public void createOrUpdateTimeKeepingSheet(String action){
		if (ObjectUtils.isEmpty(listApprovePeople)) {
			CommonFaces.showMessageWarn("Phòng này chưa cài đặt thông tin người duyệt!");
			return;
		}

		PrimeFaces current = PrimeFaces.current();

		if (action.equals("ADD")){
			for (TimekeepingSheetData timeKeepingSheetData: listUpdateTimeKeepingSheet){
				timeKeepingSheetData.setState("0");
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(timeKeepingSheetData.getKeepingDate());
				calendar.add(Calendar.DATE, 1);
				timeKeepingSheetData.setKeepingDate(calendar.getTime());

				StringBuilder imagePath = new StringBuilder();
				StringBuilder imageUrl = new StringBuilder();
				handleUploadImage(imagePath, imageUrl, timeKeepingSheetData.getUploadedImage());

				timeKeepingSheetData.setImagePath(imagePath.toString());
				timeKeepingSheetData.setTimeKeepingImage(imageUrl.toString());
				timeKeepingSheetData.setPeopleId(updateTimeKeepingSheet.getPeopleId());
				timeKeepingSheetData.setApprovedBy(updateTimeKeepingSheet.getApprovedBy());
				timeKeepingSheetData.setSupervisor(updateTimeKeepingSheet.getSupervisor());
				if (!ObjectUtils.isEmpty(approvePeople)) {
					timeKeepingSheetData.setRuleId(approvePeople.getRuleId());
				}

				if (!ObjectUtils.isEmpty(approvalFlow)) {
					if (!ObjectUtils.isEmpty(approvalFlow.getApprovalLevel())) {
						timeKeepingSheetData.setApprovalLevel(approvalFlow.getApprovalLevel());
					}

					if (!ObjectUtils.isEmpty(approvalFlow.getApprovalLevel1())) {
						timeKeepingSheetData.setRoleLevel1(approvalFlow.getApprovalLevel1());
					}

					if (!ObjectUtils.isEmpty(approvalFlow.getApprovalLevel2())) {
						timeKeepingSheetData.setRoleLevel2(approvalFlow.getApprovalLevel2());
					}
				}

				timeKeepingSheetData.setCreatedAt(new Date());

				if (!ObjectUtils.isEmpty(timeKeepingSheetData.getTimeKeepingDateCbxValues())){
					for (String value : timeKeepingSheetData.getTimeKeepingDateCbxValues()){
						TimekeepingDate timekeepingDate = new TimekeepingDate();
						timekeepingDate.setDate(value);
						timeKeepingSheetData.getTimeKeepingDate().add(timekeepingDate);
					}
				}

				try{
					ObjectMapper mapper = new ObjectMapper();
					String jsonData = mapper.writeValueAsString(timeKeepingSheetData);
					System.out.println(jsonData);
				}catch(Exception e){
					e.printStackTrace();
				}

				String response = timeKeepingSheetClient.addNewTimeKeepingSheet(timeKeepingSheetData);

				if (!response.equals("200")){
					CommonFaces.showMessageWarn("Ngày giải trình đã tồn tại trên hệ thống!");
					return;
				}
			}

			CommonFaces.showGrowlInfo("Tạo đề xuất giải trình thành công!");
			current.executeScript("PF('dagAddTimeKeeping').hide();");
		} else if (action.equals("EDIT")) {
			TimekeepingSheetData timeKeepingSheetData = listUpdateTimeKeepingSheet.get(0);

			if (!ObjectUtils.isEmpty(timeKeepingSheetData.getUploadedImage())){
				StringBuilder imagePath = new StringBuilder();
				StringBuilder imageUrl = new StringBuilder();
				handleUploadImage(imagePath, imageUrl, timeKeepingSheetData.getUploadedImage());
				timeKeepingSheetData.setImagePath(imagePath.toString());
				timeKeepingSheetData.setTimeKeepingImage(imageUrl.toString());
			}

			timeKeepingSheetData.setPeopleId(updateTimeKeepingSheet.getPeopleId());

			if (!ObjectUtils.isEmpty(timeKeepingSheetData.getTimeKeepingDateCbxValues())){
				for (String value : timeKeepingSheetData.getTimeKeepingDateCbxValues()){
					TimekeepingDate timekeepingDate = new TimekeepingDate();
					timekeepingDate.setDate(value);
					timeKeepingSheetData.getTimeKeepingDate().add(timekeepingDate);
				}
			}

			ObjectMapper mapper = new ObjectMapper();
			try {
				System.out.println(mapper.writeValueAsString(timeKeepingSheetData));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}

			String response =  timeKeepingSheetClient.editTimeKeepingSheet(timeKeepingSheetData);

			if (!response.equals("200")){
				CommonFaces.showMessageWarn("Ngày giải trình đã tồn tại trên hệ thống!");
				return;
			}

			CommonFaces.showGrowlInfo("Sửa đề xuất giải trình thành công!");
			current.executeScript("PF('dagEditTimeKeeping').hide();");
		}

		initTblTimeKeepkingExplanation();
	}

	public void deleteListUpdateTimeKeepingSheetItem(TimekeepingSheetData item){
		Iterator itr = listUpdateTimeKeepingSheet.iterator();

		while (itr.hasNext()) {
			TimekeepingSheetData searchItem = (TimekeepingSheetData)itr.next();
			if (searchItem.equals(item))
				itr.remove();
		}
	}

	public void grantDeleteObject(TimekeepingSheetData deleteObj){
    	this.updateTimeKeepingSheet = deleteObj;
    }

	public void delete(){
    	TimekeepingExplanationDao.deleteTimeKeeping(updateTimeKeepingSheet.getAutoid());
    	initTblTimeKeepkingExplanation();
	}
}
