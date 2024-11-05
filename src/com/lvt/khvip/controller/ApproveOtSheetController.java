/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.lvt.khvip.client.ApprovePeopleClient;
import com.lvt.khvip.client.OtSheetClient;
import com.lvt.khvip.client.dto.OtSheetData;
import com.lvt.khvip.client.dto.OtSheetData;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.*;
import com.lvt.khvip.entity.*;
import com.lvt.khvip.model.OtSheetDataModel;
import com.lvt.khvip.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Getter
@Setter
@ManagedBean
@SessionScoped
public class ApproveOtSheetController implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ApproveOtSheetController.class);

	private List<People> listPeopleForSearch;
	private List<People> listApprovePeople;
	private Map<String,People> peopleByFullName = new HashMap();
	private Map<String,People> peopleByPeopleId = new HashMap();

	private List<TimekeepingSheetExcel> listTimekeepingSheet;
	private List<Groups> listGroupForSearch;
	private OtSheetData searchOtSheet = new OtSheetData();

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
    private OtSheetClient otSheetClient = new OtSheetClient();
    private ApprovePeopleClient approvePeopleClient = new ApprovePeopleClient();
	private ApprovePeople approvePeople;

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
	private List<OtSheetDate> listUpdateTimeKeepingSheet = new ArrayList();
	private String noRecordMsg = "không có dữ liệu...";
	private List<ApprovalFlow> listApprovalFlow = new ArrayList();

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

			listApprovalFlow = TimekeepingExplanationDao.getApprovalFlowByRole(user.getRole().getName(),
					depId, Constants.ApprovalFLow.OT_APPROVAL);

			for (ApprovalFlow approvalFlow: listApprovalFlow) {
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

    public ApproveOtSheetController() {
    	groupsDao = new GroupsDao();
		initListPeopleForSearch();
		initUser();
		initListApprovalFlow();
		initTblOtSheet();
    	initListGroupForSearch();
    	initTimeKeepingDate();
	}
	
	private void logMessage(Severity severity, String text) {
		FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(severity, text, null));
	}
	
	public void changeState(String newState) {
		ApproveTimeKeeping approveTimeKeeping = new ApproveTimeKeeping();
		approveTimeKeeping.setState(Integer.parseInt(newState));
		List<Integer> approvalIds = new ArrayList();
		approvalIds.add(updateOtSheet.getAutoid());
		approveTimeKeeping.setApprovalIds(approvalIds);
		approveTimeKeeping.setApprovedBy(" ");

		if (!StringUtils.isEmpty(selectedApproval)) {
			if (selectedApproval.equals(Constants.ApprovalFLow.level1)) {
				approveTimeKeeping.setApprovedBy(user.getPeopleId());
				approveTimeKeeping.setApprovedAt(new Date());
			} else if (selectedApproval.equals(Constants.ApprovalFLow.level2)) {
				approveTimeKeeping.setApprovalByLevel2(user.getPeopleId());
				approveTimeKeeping.setApprovedAtLevel2(new Date());
			}
		}

		otSheetClient.approve(approveTimeKeeping);
		showApprovalMessage(newState);
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('dagOtDetail').hide();");

		initTblOtSheet();
	}
	
	public void initSelectedOtSheet(OtSheetData otSheetData) {
		this.updateOtSheet = otSheetData;
		OtSheetData condition = new OtSheetData();
		condition.setAutoid(updateOtSheet.getAutoid());
		OtSheetData timeKeepingSheet = otSheetClient.getOtSheet(condition);

		updateOtSheet.setSupervisor(timeKeepingSheet.getSupervisor());

		if (StringUtils.isEmpty(updateOtSheet.getApprovalByLevel2()) && !ObjectUtils.isEmpty(listApprovePeople)
				&& listApprovePeople.size() == 1 && updateOtSheet.getApprovalLevel().equals(2)) {
			updateOtSheet.setApprovalByLevel2(listApprovePeople.get(0).getPeopleId());
		}

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/YYYY");
		for (OtSheetDate otSheetDate : otSheetData.getOtSheetDates()){
			try {
				otSheetDate.setOtDateTypeDate(simpleDateFormat.parse(otSheetDate.getOtDate()));
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		listUpdateTimeKeepingSheet = otSheetData.getOtSheetDates();
    }
	
    public void initTblOtSheet() {
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

			} else if (user.getRole().getName().equals(Constants.RoleConstants.QLNSP)) {
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

	public boolean checkValidSelectedManyRecord(String newState, List<Integer> approvalIds) {
		if (approvalIds == null) {
			approvalIds = new ArrayList();
		}

		for (OtSheetData otSheetData : dataModel.getDatasource()) {
			if (!ObjectUtils.isEmpty(otSheetData.getSelectedCheckbox()) && otSheetData.getSelectedCheckbox()) {
				if (newState.equals("2") || newState.equals("3")) {
					if (otSheetData.getState().equals("3") || otSheetData.getState().equals("2")) {
						CommonFaces.showMessageWarn("Chỉ chọn những đề xuất Chưa phê duyệt hoặc Duyệt cấp 1!");
						return false;
					}
				} else if (newState.equals("1")) {
					if (otSheetData.getState().equals("3") || otSheetData.getState().equals("2") || otSheetData.getState().equals("1")) {
						CommonFaces.showMessageWarn("Chỉ chọn những đề xuất Chưa phê duyệt!");
						return false;
					}
				}

				approvalIds.add(otSheetData.getAutoid());
			}
		}

		if (approvalIds.isEmpty()) {
			CommonFaces.showMessageWarn("Chưa chọn bản ghi nào!");
			return false;
		}


		if (newState.equals("1")){
			changeStateMany("1");
		}

		return true;
	}

	public void showApprovalMessage(String state) {
		if (state.equals("1")) {
			CommonFaces.showGrowlInfo("Duyệt cấp 1 thành công!");

		} else if (state.equals("2")) {
			CommonFaces.showGrowlInfo("Phê duyệt thành công!");

		} else if (state.equals("3")) {
			CommonFaces.showGrowlInfo("Từ chối thành công!");
		}
	}

	public void changeStateMany(String newState){
		List<Integer> approvalIds = new ArrayList();

		if (!checkValidSelectedManyRecord(newState, approvalIds)) {
			return;
		}

		ApproveTimeKeeping approveTimeKeeping = new ApproveTimeKeeping();
		approveTimeKeeping.setState(Integer.parseInt(newState));
		approveTimeKeeping.setApprovalIds(approvalIds);

		if (!StringUtils.isEmpty(selectedApproval)) {
			if (selectedApproval.equals(Constants.ApprovalFLow.level1)) {
				approveTimeKeeping.setApprovedBy(user.getPeopleId());
				approveTimeKeeping.setApprovedAt(new Date());
			} else if (selectedApproval.equals(Constants.ApprovalFLow.level2)) {
				approveTimeKeeping.setApprovalByLevel2(user.getPeopleId());
				approveTimeKeeping.setApprovedAtLevel2(new Date());
			}
		}

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			String json = ow.writeValueAsString(approveTimeKeeping);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		OtSheetDao.updateApprovalLevel2ByIds(approveTimeKeeping);
		OtSheetDao.updateState(approveTimeKeeping);
		showApprovalMessage(newState);
		initTblOtSheet();
	}

	public void approveMany(String buttonCode) {
		List<OtSheetData> listItemForDeny = new ArrayList();
		List<OtSheetData> listItemForApproveLevel1 = new ArrayList();
		List<OtSheetData> listItemForApproveLevel2 = new ArrayList();

		boolean selectedRecord = false;
		for (OtSheetData OtSheetData : dataModel.getDatasource()) {
			if (ObjectUtils.isEmpty(OtSheetData.getSelectedCheckbox())
					|| (!ObjectUtils.isEmpty(OtSheetData.getSelectedCheckbox()) && !OtSheetData.getSelectedCheckbox())) {
				continue;
			}
			selectedRecord = true;

			if (buttonCode.equals("DENY")) {
				if (OtSheetData.getState().equals("0")) {
					listItemForDeny.add(OtSheetData);
				}
			} else if (buttonCode.equals("APPROVE")) {
				if (OtSheetData.getState().equals("0") && OtSheetData.getApprovedBy().equals(user.getPeopleId())
						&& OtSheetData.getApprovalLevel().equals(2)) {
					listItemForApproveLevel1.add(OtSheetData);

				} if (OtSheetData.getState().equals("0") && OtSheetData.getApprovedBy().equals(user.getPeopleId())
						&& OtSheetData.getApprovalLevel().equals(1)) {
					listItemForApproveLevel2.add(OtSheetData);

				} else if (OtSheetData.getState().equals("1") && OtSheetData.getApprovalLevel().equals(2)
						&& OtSheetData.getRoleLevel2().equals(user.getRole().getName())) {
					listItemForApproveLevel2.add(OtSheetData);
				}
			}
		}

		if (!selectedRecord) {
			CommonFaces.showMessageWarn("Chưa chọn bản ghi nào!");
			return;
		}

		updateStateMany("3", listItemForDeny);
		updateStateMany("1", listItemForApproveLevel1);
		updateStateMany("2", listItemForApproveLevel2);

		if (buttonCode.equals("APPROVE")) {
			if (!listItemForApproveLevel1.isEmpty() || !listItemForApproveLevel2.isEmpty()) {
				int approveTotal = listItemForApproveLevel1.size() + listItemForApproveLevel2.size();
				CommonFaces.showGrowlInfo("Phê duyệt thành công: "+approveTotal+" bản ghi!");
			} else {
				CommonFaces.showMessageWarn("Chỉ chọn những đề xuất Chưa phê duyệt hoặc Duyệt cấp 1!");
			}

		} else if (buttonCode.equals("DENY")) {
			if (!listItemForDeny.isEmpty()) {
				CommonFaces.showGrowlInfo("Từ chối thành công: "+listItemForDeny.size()+" bản ghi!");
			} else {
				CommonFaces.showGrowlInfo("Chỉ chọn những đề xuất Chưa phê duyệt hoặc Duyệt cấp 1!");
			}
		}

		initTblOtSheet();
	}

	public void updateStateMany(String newState, List<OtSheetData> listSelectedItems) {
		if (listSelectedItems.isEmpty()) {
			return;
		}

		List<Integer> approvalIds = new ArrayList();
		approvalIds = listSelectedItems.stream().map(OtSheetData -> OtSheetData.getAutoid()).collect(Collectors.toList());

		ApproveTimeKeeping approveTimeKeeping = new ApproveTimeKeeping();
		approveTimeKeeping.setState(Integer.parseInt(newState));
		approveTimeKeeping.setApprovalIds(approvalIds);

		if (!listSelectedItems.isEmpty()) {
			if (listSelectedItems.get(0).getApprovedBy().equals(user.getPeopleId())) {
				approveTimeKeeping.setApprovedBy(user.getPeopleId());
				approveTimeKeeping.setApprovedAt(new Date());
			} else if (!StringUtils.isEmpty(listSelectedItems.get(0).getRoleLevel2())
					&& listSelectedItems.get(0).getRoleLevel2().equals(user.getRole().getName())) {
				approveTimeKeeping.setApprovalByLevel2(user.getPeopleId());
				approveTimeKeeping.setApprovedAtLevel2(new Date());
			}
		}

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		try {
			String json = ow.writeValueAsString(approveTimeKeeping);
			System.out.println(json);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		OtSheetDao.updateApprovalLevel2ByIds(approveTimeKeeping);
		OtSheetDao.updateState(approveTimeKeeping);

	}

	public boolean renderDenyButton() {
		if (Objects.isNull(updateOtSheet)) {
			return false;
		}

		if (StringUtils.isEmpty(updateOtSheet.getState())) {
			return false;
		}

		if (updateOtSheet.getState().equals("1")) {
			if (!StringUtils.isEmpty(selectedApproval) && selectedApproval.equals(Constants.ApprovalFLow.level1)) {
				return false;
			}
		}

		if (updateOtSheet.getState().equals("2") || updateOtSheet.getState().equals("3")) {
			return false;
		} else {
			return true;
		}
	}

	public boolean renderButton(String buttonCode){
		if (Objects.isNull(updateOtSheet)){
			return false;
		}

		if (buttonCode.equals(Constants.ApprovalFLow.NEXT_APPROVAL)){
			if (!StringUtils.isEmpty(updateOtSheet.getState()) && !updateOtSheet.getState().equals("0")){
				return false;
			}
		}

		if (buttonCode.equals(Constants.ApprovalFLow.APPROVAL)){
			if (!StringUtils.isEmpty(updateOtSheet.getState()) && (updateOtSheet.getState().equals("2")
					|| updateOtSheet.getState().equals("3"))){
				return false;
			}
		}

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
		if (ObjectUtils.isEmpty(listApprovalFlow) || (!ObjectUtils.isEmpty(listApprovalFlow) && listApprovalFlow.isEmpty())) {
			return false;

		} else if (buttonCode.equals(Constants.ApprovalFLow.APPROVAL)){
			if (listApprovalFlow.size() == 1){
				ApprovalFlow approvalFlow = listApprovalFlow.get(0);

				if (approvalFlow.getApprovalLevel().equals(1)){
					return true;
				} else if (approvalFlow.getApprovalLevel().equals(2)) {
					if (approvalFlow.getApprovalLevel2().equals(user.getRole().getName())) {
						return true;
					} else {
						return false;
					}
				}
			}
		} else if (buttonCode.equals(Constants.ApprovalFLow.NEXT_APPROVAL)) {
			if (listApprovalFlow.size() == 1){
				ApprovalFlow approvalFlow = listApprovalFlow.get(0);

				if (approvalFlow.getApprovalLevel().equals(1)){
					return false;
				} else if (approvalFlow.getApprovalLevel().equals(2)) {
					if (approvalFlow.getApprovalLevel1().equals(user.getRole().getName())) {
						return true;
					} else {
						return false;
					}
				}
			}
		}

		return true;
	}

}
