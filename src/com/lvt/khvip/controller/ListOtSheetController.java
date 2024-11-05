/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.lvt.khvip.client.OtSheetClient;
import com.lvt.khvip.client.dto.OtSheetData;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.GroupsDao;
import com.lvt.khvip.dao.PeopleDao;
import com.lvt.khvip.dao.TimekeepingDateDao;
import com.lvt.khvip.dao.TimekeepingExplanationDao;
import com.lvt.khvip.entity.*;
import com.lvt.khvip.model.OtSheetDataModel;
import com.lvt.khvip.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Getter
@Setter
@ManagedBean
@SessionScoped
public class ListOtSheetController implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ListOtSheetController.class);

	private List<People> listPeopleForSearch;
	private List<String> listApprovePeople;
	private Map<String,People> peopleByFullName = new HashMap();
	private Map<String,People> peopleByPeopleId = new HashMap();

	private List<OtSheetData> listTimekeepingSheet;
	private List<Groups> listGroupForSearch;
	private OtSheetData searchOtSheet = new OtSheetData();

	private GroupsDao groupsDao;
	private ExcelUtils excelUtils = new ExcelUtils();
	private UploadedFile uploadedExcel;
	private UploadedFile signedFile;
	private UploadedFile uploadedImage;
	private String uploadExcelStatus;
	private String errorSummary;
	private byte[] attachedErrorFileByteArr;
    private StreamedContent content ;
    private StreamedContent exportExcelContent ;
    private String title;
    private boolean disableErrorLink = true;
    private OtSheetClient otSheetClient = new OtSheetClient();
    private OtSheetDataModel dataModel;
    private Date fromDate;
    private Date toDate;
	private String rootPath = ConfProperties.getProperty("root.path");
	private String imageFolder = ConfProperties.getProperty("image.folder");
	private String imageRootUrl = ConfProperties.getProperty("image.root.url");
	private OtSheetData updateOtSheet = new OtSheetData();
	private List<TimekeepingDate> listTimekeepingDate = new ArrayList();
	private User user;
	private Map<Integer,String> getApprovalFlowByGroupId = new HashMap();
	private List<String> approvalList = new ArrayList();
	private String selectedApproval = "";
	private List<OtSheetDate> listOtSheet = new ArrayList();
	private String noRecordMsg = "không có dữ liệu...";

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

		if (!ObjectUtils.isEmpty(user) && !StringUtils.isEmpty(user.getPeopleId())){
			People people = peopleByPeopleId.get(user.getPeopleId());

			if (!ObjectUtils.isEmpty(people) && !ObjectUtils.isEmpty(people.getFullName())){
				user.setFullName(people.getFullName());
			}
		}
	}

	public void exportExcel() {
		if (ObjectUtils.isEmpty(dataModel.getDatasource())){
			return;
		}

		ExcelTemplate excelTemplate = excelUtils.getExcelTemplate(Constants.ExcelTemplateCodes.OT_SHEET_EXPORT);

		byte[] exportDataByteArr = excelUtils.exportExcel(dataModel.getDatasource(), OtSheetData.class, excelTemplate);

		String fileName = "ot_sheet.xlsx";
		InputStream downloadFileInputStream = new ByteArrayInputStream(exportDataByteArr);
		exportExcelContent = DefaultStreamedContent.builder()
				.name(fileName)
				.stream(() -> downloadFileInputStream).build();

	}

	public void saveExcelData(List<OtSheetData> listTimeKeepingSheet, Map<Integer,
			StringBuilder>  mapExcelError, Set<String>  errorSummarySet) {

		listTimeKeepingSheet.stream().forEach(otSheetData -> {
			otSheetData.setApprovedBy(otSheetData.getApprovedBy()!=null? otSheetData.getApprovedBy():"");
			otSheetData.setApprovalByLevel2(otSheetData.getApprovalByLevel2()!=null? otSheetData.getApprovalByLevel2():"");
			otSheetData.setProject(otSheetData.getProject()!=null? otSheetData.getProject():"");
			otSheetData.setAutoid(otSheetData.getAutoid()!=null? otSheetData.getAutoid():0);
			otSheetData.setWorkingNotes(otSheetData.getWorkingNotes()!=null? otSheetData.getWorkingNotes():"");

			if (!StringUtils.isEmpty(otSheetData.getStateValue())){
				if (otSheetData.getStateValue().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.NEXT_APPROVED_STATUS)){
					otSheetData.setState("1");

				} else if (otSheetData.getStateValue().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.APPROVED_STATUS)){
					otSheetData.setState("2");

				}  else if (otSheetData.getStateValue().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.NEW_STATUS)){
					otSheetData.setState("0");

				} else {
					otSheetData.setState("3");
				}
			}
		});

		ImportExcelDto importExcelDto = new ImportExcelDto();
		importExcelDto.setObjectImport(listTimeKeepingSheet);
		importExcelDto.setImportBy(SessionUtils.getUserName());
		importExcelDto.setTitle(title);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			String json = ow.writeValueAsString(importExcelDto);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		//TimekeepingExplanationDao.importExcel(importExcelDto);
		ImportExcelDto<OtSheetData> importResult = otSheetClient.importExcel(importExcelDto);
		Map<String, OtSheetData> mListErrorAfter = new HashMap<>();

		if (!ObjectUtils.isEmpty(importResult) && !ObjectUtils.isEmpty(importResult.getObjectImport())) {
			importResult.getObjectImport().forEach(item -> {
				if (!com.lvt.khvip.util.StringUtils.isEmpty(item.getErrorCode()) && !"S".equals(item.getErrorCode())) {
					String key = item.getPeopleId() + "_" + item.getGroupId();
					if (!mListErrorAfter.containsKey(key)) {
						mListErrorAfter.put(key, item);
					}
				}
			});
		}

		if (mListErrorAfter.size() > 0) {
			if (mapExcelError == null) {
				mapExcelError = new HashMap<>();
			}
			for (int i = 0; i < listTimeKeepingSheet.size(); i++) {
				OtSheetData item = listTimeKeepingSheet.get(i);
				String key = item.getPeopleId() + "_" + item.getGroupId();
				if (mListErrorAfter.containsKey(key)) {
					OtSheetData itemError = mListErrorAfter.get(key);
					Integer keyMapError = Integer.valueOf(i);
					if (mapExcelError.containsKey(keyMapError)) {
						mapExcelError.get(keyMapError).append(itemError.getErrorMessage());
						errorSummarySet.add(MessageProvider.getValue("upload.excel.email.saving.failed", null));
					} else {
						mapExcelError.put(keyMapError, new StringBuilder(itemError.getErrorMessage()));
						errorSummarySet.add(MessageProvider.getValue("upload.excel.email.saving.failed", null));
					}
				}
			}
		}


	}

	public void downloadFile() {
		try {
			String fileName = ""
					+ "ot_sheet_error_"+new Date().getTime()+".xlsx";
			InputStream downloadFileInputStream = new ByteArrayInputStream(attachedErrorFileByteArr);
			DefaultStreamedContent content = DefaultStreamedContent.builder()
					.name(fileName)
					.stream(() -> downloadFileInputStream).build();
			this.content = content;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	public void initListApprovalFlow(){
    	if (ObjectUtils.isEmpty(user)){
    		return;
		}

    	if (!ObjectUtils.isEmpty(user.getGroupId()) && !ObjectUtils.isEmpty(user.getRole())){
			Integer depId = 0;
			if (user.getRole().getName().equals(Constants.RoleConstants.GROUP_LEADER)) {
				depId = user.getGroupId();
			} else {
				depId = user.getDepId();
			}

			List<ApprovalFlow> listApprovalFlow =
					TimekeepingExplanationDao.getApprovalFlowByRole(user.getRole().getName(),
							depId, Constants.ApprovalFLow.OT_APPROVAL);

			for (ApprovalFlow approvalFlow: listApprovalFlow){
				//getApprovalFlowByGroupId.put(approvalFlow.getGroupId(),approvalFlow);

				if (approvalFlow.getApprovalLevel().equals(1)) {
					getApprovalFlowByGroupId.put(approvalFlow.getGroupId(),Constants.ApprovalFLow.APPROVAL);
					approvalList.add(Constants.ApprovalFLow.level1);
				} else {
					if (approvalFlow.getApprovalLevel1().equals(user.getRole().getName())){
						getApprovalFlowByGroupId.put(approvalFlow.getGroupId(),Constants.ApprovalFLow.NEXT_APPROVAL);
						approvalList.add(Constants.ApprovalFLow.level1);
					} else if (approvalFlow.getApprovalLevel2().equals(user.getRole().getName())){
						getApprovalFlowByGroupId.put(approvalFlow.getGroupId(),Constants.ApprovalFLow.APPROVAL);
						approvalList.add(Constants.ApprovalFLow.level2);
					}
				}

				if (!approvalList.isEmpty()) {
					selectedApproval = approvalList.get(0);
				}
			}
		}

		if (approvalList.isEmpty() && !user.getUsername().equalsIgnoreCase(Constants.RoleConstants.ADMIN) &&
				!user.getRole().getName().equals(Constants.RoleConstants.QLNSP)) {
			noRecordMsg = "tài khoản này không có quyền duyệt!";
		}
	}

    public void initTimeKeepingDate(){
		listTimekeepingDate = TimekeepingDateDao.getListTimeKeepingDate(null);
	}

    public ListOtSheetController() {
    	groupsDao = new GroupsDao();
		initListPeopleForSearch();
		initUser();
		initListApprovalFlow();
		initTblOtSheet();
    	initListGroupForSearch();
    	initTimeKeepingDate();
	}
	
	public void showResultDialog(Set<String> errorSummarySet) {
	    if (errorSummarySet.isEmpty()) {
	    	uploadExcelStatus = MessageProvider.getValue("upload.excel.success", null);
	    	disableErrorLink = false;
	    } else {
	    	uploadExcelStatus = MessageProvider.getValue("upload.excel.fail", null);
	    	
			StringBuilder errorSummaryVl = new StringBuilder();
			errorSummarySet.forEach(cellError -> {
				errorSummaryVl.append(cellError);
			});
			
			errorSummary = errorSummaryVl.toString();
	    }	
	    PrimeFaces current = PrimeFaces.current();
	    
		current.executeScript("PF('dagUploadExcel').hide();");
		current.executeScript("PF('dagUploadExcelStatus').show();");
	}
	
	private void logMessage(Severity severity, String text) {
		FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(severity, text, null));
	}

	public void closeDlgAddNewTimeKeepingSheet() {
		updateOtSheet = new OtSheetData();
		listOtSheet.clear();
	}

	public void uploadExcelFile() {
		try {
			if (uploadedExcel == null || StringUtils.isEmpty(title)) {
				return;
			}

			InputStream inputStream = uploadedExcel.getInputStream();
			byte[] byteArr = IOUtils.toByteArray(inputStream);

			Map<Integer, StringBuilder> mapExcelError = new HashMap();
			Set<String> errorSummarySet = new HashSet();
			ExcelTemplate excelTemplate = excelUtils.getExcelTemplate(Constants.ExcelTemplateCodes.OT_SHEET_UPLOAD);
			ExcelResponseMessage excelResponseMessage = new ExcelResponseMessage();
			String fileName = uploadedExcel.getFileName() != null ? uploadedExcel.getFileName() : "";

			if (!excelUtils.validFileType(fileName, excelResponseMessage, errorSummarySet)){
				disableErrorLink = false;
				showResultDialog(errorSummarySet);
				return;
			}

			Workbook workbook = excelUtils.initWorkbook(byteArr, fileName);

			Sheet sheet = workbook.getSheetAt(excelTemplate.getSheetSeqNo());

			if (excelUtils.checkEmptyFile(sheet, excelTemplate.getStartRow(), excelResponseMessage, errorSummarySet)){
				attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError, excelTemplate);
				disableErrorLink = false;
				showResultDialog(errorSummarySet);
				return;
			}

			validateAndSaveExcelData(workbook, sheet, fileName, mapExcelError, errorSummarySet, excelTemplate);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void validateAndSaveExcelData(Workbook workbook, Sheet sheet, String fileName, Map<Integer,
			StringBuilder> mapExcelError, Set<String> errorSummarySet, ExcelTemplate excelTemplate) {
		List<OtSheetData> listTimeKeepingSheet = excelUtils.parseExcelToDto(OtSheetData.class,  sheet, fileName, excelTemplate, mapExcelError, errorSummarySet);

		//checkValidDescription(listTimeKeepingSheet, mapExcelError, errorSummarySet);
		//checkValidPeopleId(listTimeKeepingSheet, mapExcelError, errorSummarySet);
		//checkValidPeopleFullName(listTimeKeepingSheet, mapExcelError, errorSummarySet);
		//setAutoIdForExcelItem(listTimeKeepingSheet, mapExcelError, errorSummarySet);

		if (!errorSummarySet.isEmpty() || !mapExcelError.isEmpty()) {
			attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError, excelTemplate);
			showResultDialog(errorSummarySet);
		} else if (errorSummarySet.isEmpty() && mapExcelError.isEmpty()) {
			saveExcelData(listTimeKeepingSheet, mapExcelError, errorSummarySet);

			//if there is error while saving
			if (!mapExcelError.isEmpty() || !errorSummarySet.isEmpty()) {
				attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError, excelTemplate);
				showResultDialog(errorSummarySet);
			} else if (mapExcelError.isEmpty() && errorSummarySet.isEmpty()) {
				CommonFaces.showGrowlInfo("Upload file excel thành công!");
				disableErrorLink = false;
				showResultDialog(errorSummarySet);
				initTblOtSheet();
			}
		}
	}
	
	public void changeState(String newState) {
		ApproveTimeKeeping approveTimeKeeping = new ApproveTimeKeeping();
		approveTimeKeeping.setState(Integer.parseInt(newState));
		List<Integer> approvalIds = new ArrayList();
		approvalIds.add(updateOtSheet.getAutoid());
		approveTimeKeeping.setApprovalIds(approvalIds);
		approveTimeKeeping.setApprovedBy(updateOtSheet.getApprovedBy());
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			String json = ow.writeValueAsString(approveTimeKeeping);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		otSheetClient.approve(approveTimeKeeping);
		//TimekeepingExplanationDao.updateState(approveTimeKeeping);

		initTblOtSheet();
	}
	
	public static User getSessionUser() {
		User user = null;
		try {
			HttpSession session = SessionUtils.getSession();
			user = (User) session.getAttribute("user-info");
		} catch (Exception ex) {
			return null;
		}
		return user;
	}
	
	public void initSelectedOtSheet(OtSheetData otSheetData) {
		this.updateOtSheet = otSheetData;
		OtSheetData condition = new OtSheetData();
		condition.setAutoid(updateOtSheet.getAutoid());
		OtSheetData timeKeepingSheet = otSheetClient.getOtSheet(condition);

		updateOtSheet.setSupervisor(timeKeepingSheet.getSupervisor());

		if (!ObjectUtils.isEmpty(updateOtSheet.getGroupId())){
			listApprovePeople = otSheetClient.getListManager(updateOtSheet.getGroupId());
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/YYYY");
		for (OtSheetDate otSheetDate : otSheetData.getOtSheetDates()){
			try {
				otSheetDate.setOtDateTypeDate(simpleDateFormat.parse(otSheetDate.getOtDate()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		listOtSheet = otSheetData.getOtSheetDates();
    }
	
    public void initTblOtSheet() {
		if (approvalList.isEmpty() &&
				!user.getUsername().equalsIgnoreCase(Constants.RoleConstants.ADMIN) &&
				!user.getRole().getName().equals(Constants.RoleConstants.QLNSP)) {
			return;
		}

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

		searchOtSheet.setFromDate(convertedFromDate);
		searchOtSheet.setToDate(convertedToDate);

    	if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getPeopleId())){
			if (!user.getUsername().equalsIgnoreCase(Constants.RoleConstants.ADMIN) && !user.getRole().getName().equals(Constants.RoleConstants.QLNSP)) {
				Integer depId = 0;
				if (user.getRole().getName().equals(Constants.RoleConstants.GROUP_LEADER)) {
					depId = user.getGroupId();
				} else {
					depId = user.getDepId();
				}

				searchOtSheet.setApprovedBy(user.getPeopleId());
				searchOtSheet.setRoleLevel2(user.getRole().getName());
				searchOtSheet.setOrgId(depId);

			} else if (user.getRole().getName().equals(Constants.RoleConstants.QLNSP)){
				searchOtSheet.setGroupId(user.getGroupId());
			}

			dataModel = new OtSheetDataModel("tblListApprove", searchOtSheet, otSheetClient);
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
    	initTblOtSheet();
    }

	public void onChangePeopleId(AjaxBehaviorEvent event) {
		if (ObjectUtils.isEmpty(((UIOutput) event.getSource()).getValue())) {
			updateOtSheet.setFullName(null);
			updateOtSheet.setGroupName(null);
			return;
		}

		String peopleId = ((UIOutput) event.getSource()).getValue().toString();

		People people = peopleByPeopleId.get(peopleId);

		if (!ObjectUtils.isEmpty(people)){
			updateOtSheet.setFullName(people.getFullName());
			updateOtSheet.setGroupId(people.getGroupId());
			updateOtSheet.setGroupName(people.getGroupName());
		}

		if (!ObjectUtils.isEmpty(updateOtSheet.getGroupId())){
			listApprovePeople = otSheetClient.getListManager(updateOtSheet.getGroupId());
		}
	}

	public void changeStateMany(String newState){

		List<Integer> approvalIds = new ArrayList();

		for (OtSheetData timekeepingSheetData: dataModel.getDatasource()){
			if (!ObjectUtils.isEmpty(timekeepingSheetData.getSelectedCheckbox()) && timekeepingSheetData.getSelectedCheckbox()){
				approvalIds.add(timekeepingSheetData.getAutoid());
			}
		}

		if (approvalIds.isEmpty()){
			return;
		}

		ApproveTimeKeeping approveTimeKeeping = new ApproveTimeKeeping();
		approveTimeKeeping.setState(Integer.parseInt(newState));
		approveTimeKeeping.setApprovalIds(approvalIds);
		approveTimeKeeping.setApprovedBy(" ");
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			String json = ow.writeValueAsString(approveTimeKeeping);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		//timeKeepingSheetClient.approve(approveTimeKeeping);
		TimekeepingExplanationDao.updateState(approveTimeKeeping);



		initTblOtSheet();
	}

	public boolean renderButton(String buttonCode){
    	String buttonCodeFromMap = getApprovalFlowByGroupId.get(updateOtSheet.getGroupId());

    	if (StringUtils.isEmpty(buttonCodeFromMap)) {
    		return false;
		}

		if (buttonCode.equals(buttonCodeFromMap)){
			return true;
		} else {
			return false;
		}
	}

	public boolean renderApproveManyButton(String buttonCode){

    	if (buttonCode.equals(Constants.ApprovalFLow.APPROVAL)){
			if (selectedApproval.equals(Constants.ApprovalFLow.level2)){
				return true;
			} else {
				return false;
			}
		} else if (buttonCode.equals(Constants.ApprovalFLow.NEXT_APPROVAL)) {
			if (selectedApproval.equals(Constants.ApprovalFLow.level1)){
				return true;
			} else {
				return false;
			}
		}

    	return false;
	}

	public void closeDlgUploadStatus() {
		reset();
	}

}