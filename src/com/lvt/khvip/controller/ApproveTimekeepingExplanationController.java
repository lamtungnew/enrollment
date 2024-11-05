/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.lvt.khvip.client.ApprovePeopleClient;
import com.lvt.khvip.client.TimeKeepingSheetClient;
import com.lvt.khvip.client.dto.TimekeepingSheetData;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.*;
import com.lvt.khvip.entity.*;
import com.lvt.khvip.model.TimeKeepingSheetDataModel;
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
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import java.io.*;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Getter
@Setter
@ManagedBean
@SessionScoped
public class ApproveTimekeepingExplanationController implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ApproveTimekeepingExplanationController.class);

    private List<People> listPeopleForSearch;
    private List<People> listApprovePeople;
    private ApprovePeople approvePeople;
    private Map<String, People> peopleByFullName = new HashMap();
    private Map<String, People> peopleByPeopleId = new HashMap();

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
    private StreamedContent content;
    private StreamedContent exportExcelContent;
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
    private Map<Integer, String> getApprovalFlowByGroupId = new HashMap();
    private List<String> approvalList = new ArrayList();
    private String selectedApproval = "";
    private String noRecordMsg = "không có dữ liệu...";
    private List<ApprovalFlow> listApprovalFlow;

    public void reset() {
        uploadExcelStatus = "";
        errorSummary = "";
        content = null;
        disableErrorLink = true;
        title = "";
        uploadedExcel = null;

    }

    public void checkApprovePermission() {
        if (approvalList.isEmpty()) {
            CommonFaces.showMessageWarn("tài khoản này không có quyền duyệt!");
            PrimeFaces.current().ajax().update("mainForm:growl");
        }
    }

    public void initUser() {
        user = (User) SessionUtils.getSession().getAttribute("user-info");

        if (!ObjectUtils.isEmpty(user) && !StringUtils.isEmpty(user.getPeopleId())) {
            People people = peopleByPeopleId.get(user.getPeopleId());

            if (!ObjectUtils.isEmpty(people) && !ObjectUtils.isEmpty(people.getFullName())) {
                user.setFullName(people.getFullName());
            }
        }
    }

    public void initListApprovalFlow() {
        if (ObjectUtils.isEmpty(user)) {
            return;
        }

        if (!ObjectUtils.isEmpty(user.getGroupId()) && !ObjectUtils.isEmpty(user.getRole())) {
            Integer depId = 0;
            if (user.getRole().getName().equals(Constants.RoleConstants.GROUP_LEADER)) {
                depId = user.getGroupId();
            } else {
                depId = user.getDepId();
            }

            listApprovalFlow =
                    TimekeepingExplanationDao.getApprovalFlowByRole(user.getRole().getName(), depId, Constants.ApprovalFLow.KEEPING_APPROVAL);

            for (ApprovalFlow approvalFlow : listApprovalFlow) {
                if (approvalFlow.getApprovalLevel().equals(1)) {
                    getApprovalFlowByGroupId.put(approvalFlow.getGroupId(), Constants.ApprovalFLow.APPROVAL);
                    approvalList.add(Constants.ApprovalFLow.level1);
                } else {
                    if (approvalFlow.getApprovalLevel1().equals(user.getRole().getName())) {
                        getApprovalFlowByGroupId.put(approvalFlow.getGroupId(), Constants.ApprovalFLow.NEXT_APPROVAL);
                        approvalList.add(Constants.ApprovalFLow.level1);
                    } else if (approvalFlow.getApprovalLevel2().equals(user.getRole().getName())) {
                        getApprovalFlowByGroupId.put(approvalFlow.getGroupId(), Constants.ApprovalFLow.APPROVAL);
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

    public void initTimeKeepingDate() {
        listTimekeepingDate = TimekeepingDateDao.getListTimeKeepingDate(null);
    }

    private void checkRooth() {
        if (!rootPath.endsWith("/")) {
            rootPath += "/";
        }
        if (!imageFolder.endsWith("/")) {
            imageFolder += "/";
        }
        File folder = new File(rootPath + imageFolder);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    }

    public ApproveTimekeepingExplanationController() {
        groupsDao = new GroupsDao();
        initListPeopleForSearch();
        initUser();
        initListApprovalFlow();
        initTblTimeKeepkingExplanation();
        initListGroupForSearch();
        initTimeKeepingDate();
        checkRooth();
    }

    private void logMessage(Severity severity, String text) {
        FacesContext.getCurrentInstance().addMessage("messages", new FacesMessage(severity, text, null));
    }

    public void closeDlgUploadStatus() {
        reset();
    }

    public void closeDlgAddNewTimeKeepingSheet() {
        updateTimeKeepingSheet = new TimekeepingSheetData();
        listUpdateTimeKeepingSheet.clear();
    }

    public void changeState(String newState) {
        timeKeepingSheetClient.updateTimeKeepingSheet(updateTimeKeepingSheet);

        ApproveTimeKeeping approveTimeKeeping = new ApproveTimeKeeping();
        approveTimeKeeping.setState(Integer.parseInt(newState));
        List<Integer> approvalIds = new ArrayList();
        approvalIds.add(updateTimeKeepingSheet.getAutoid());
        approveTimeKeeping.setApprovalIds(approvalIds);

        if (!StringUtils.isEmpty(updateTimeKeepingSheet.getApprovalByLevel2()) && newState.equals("1")) {
            approveTimeKeeping.setApprovalByLevel2(updateTimeKeepingSheet.getApprovalByLevel2());
            TimekeepingExplanationDao.updateApprovalLevel2ByIds(approveTimeKeeping);
        }

        if (updateTimeKeepingSheet.getApprovedBy().equals(user.getPeopleId())) {
            approveTimeKeeping.setApprovedBy(user.getPeopleId());
            approveTimeKeeping.setApprovedAt(new Date());
        } else if (!StringUtils.isEmpty(updateTimeKeepingSheet.getRoleLevel2())
                && updateTimeKeepingSheet.getRoleLevel2().equals(user.getRole().getName())) {
            approveTimeKeeping.setApprovalByLevel2(user.getPeopleId());
            approveTimeKeeping.setApprovedAtLevel2(new Date());
        }

        //timeKeepingSheetClient.approve(approveTimeKeeping);
        TimekeepingExplanationDao.updateState(approveTimeKeeping);
        showApprovalMessage(newState);
        PrimeFaces current = PrimeFaces.current();
        current.executeScript("PF('dagExplanationDetail').hide();");
        initTblTimeKeepkingExplanation();
    }

    public void initSelectedTimeKeepingSheet(TimekeepingSheetData timeKeepingSheetData) {
        this.updateTimeKeepingSheet = timeKeepingSheetData;
        TimekeepingSheetData condition = new TimekeepingSheetData();
        condition.setAutoid(updateTimeKeepingSheet.getAutoid());
        TimekeepingSheetData timeKeepingSheet = timeKeepingSheetClient.getTimeKeepingSheet(condition);

        updateTimeKeepingSheet.setTimeKeepingDate(timeKeepingSheet.getTimeKeepingDate());
        updateTimeKeepingSheet.setSupervisor(timeKeepingSheet.getSupervisor());

        if (StringUtils.isEmpty(updateTimeKeepingSheet.getApprovalByLevel2()) && !ObjectUtils.isEmpty(listApprovePeople)
                && listApprovePeople.size() == 1 && updateTimeKeepingSheet.getApprovalLevel().equals(2)) {
            updateTimeKeepingSheet.setApprovalByLevel2(listApprovePeople.get(0).getPeopleId());
        }

        if (!ObjectUtils.isEmpty(updateTimeKeepingSheet.getTimeKeepingDate())) {
            List<String> listTimeKeepingDate = new ArrayList<>();
            listTimeKeepingDate = updateTimeKeepingSheet.getTimeKeepingDate().stream()
                    .map(timekeepingDate -> timekeepingDate.getDate()).collect(Collectors.toList());
            updateTimeKeepingSheet.setTimeKeepingDateCbxValues(listTimeKeepingDate.toArray(new String[0]));
        }

        listUpdateTimeKeepingSheet.clear();
        listUpdateTimeKeepingSheet.add(updateTimeKeepingSheet);
    }

    public void initTblTimeKeepkingExplanation() {
        if (approvalList.isEmpty() &&
                !user.getUsername().equalsIgnoreCase(Constants.RoleConstants.ADMIN) &&
                !user.getRole().getName().equals(Constants.RoleConstants.QLNSP)) {
            return;
        }

        Date convertedFromDate = null;
        if (!ObjectUtils.isEmpty(fromDate)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(fromDate);
            calendar.add(Calendar.HOUR, 00);
            calendar.add(Calendar.MINUTE, 00);
            calendar.add(Calendar.SECOND, 00);
            convertedFromDate = calendar.getTime();
        }

        Date convertedToDate = null;
        if (!ObjectUtils.isEmpty(toDate)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(toDate);
            calendar.add(Calendar.HOUR, 23);
            calendar.add(Calendar.MINUTE, 59);
            calendar.add(Calendar.SECOND, 59);
            convertedToDate = calendar.getTime();
        }

        searchTimekeepingSheet.setKeepingDateFrom(convertedFromDate);
        searchTimekeepingSheet.setKeepingDateTo(convertedToDate);

        if (!ObjectUtils.isEmpty(user) && !ObjectUtils.isEmpty(user.getPeopleId())) {
            if (!user.getUsername().equalsIgnoreCase(Constants.RoleConstants.ADMIN) && !user.getRole().getName().equals(Constants.RoleConstants.QLNSP)) {
                Integer depId = 0;
                if (user.getRole().getName().equals(Constants.RoleConstants.GROUP_LEADER)) {
                    depId = user.getGroupId();
                } else {
                    depId = user.getDepId();
                }

                searchTimekeepingSheet.setApprovedBy(user.getPeopleId());
                searchTimekeepingSheet.setRoleLevel2(user.getRole().getName());
                searchTimekeepingSheet.setOrgId(depId);

            } else if (user.getRole().getName().equals(Constants.RoleConstants.QLNSP)) {
                searchTimekeepingSheet.setGroupId(user.getGroupId());
            }

            dataModel = new TimeKeepingSheetDataModel("tblListApprove", searchTimekeepingSheet, timeKeepingSheetClient);
        }
    }

    public void initListPeopleForSearch() {
        listPeopleForSearch = PeopleDao.getListPeople();
        peopleByFullName = listPeopleForSearch.stream().collect
                (Collectors.toMap(people -> people.getFullName(), Function.identity(), (p1, p2) -> {
                    //todo
                    return null;
                }));

        peopleByPeopleId = listPeopleForSearch.stream().collect
                (Collectors.toMap(people -> people.getPeopleId(), Function.identity(), (p1, p2) -> {
                    //todo
                    return null;
                }));
    }

    public void initListGroupForSearch() {
        listGroupForSearch = groupsDao.getGroupList();
    }

    public void search() {
        initTblTimeKeepkingExplanation();
    }

    public boolean checkValidSelectedManyRecord(String newState, List<TimekeepingSheetData> approvalItems) {
        if (approvalItems == null) {
            approvalItems = new ArrayList();
        }

        for (TimekeepingSheetData timekeepingSheetData : dataModel.getDatasource()) {
            if (!ObjectUtils.isEmpty(timekeepingSheetData.getSelectedCheckbox()) && timekeepingSheetData.getSelectedCheckbox()) {
                if (newState.equals("2") || newState.equals("3")) {
                    if (timekeepingSheetData.getState().equals("3") || timekeepingSheetData.getState().equals("2")) {
                        CommonFaces.showMessageWarn("Chỉ chọn những đề xuất Chưa phê duyệt hoặc Duyệt cấp 1!");
                        return false;
                    }
                } else if (newState.equals("1")) {
                    if (timekeepingSheetData.getState().equals("3") || timekeepingSheetData.getState().equals("2") || timekeepingSheetData.getState().equals("1")) {
                        CommonFaces.showMessageWarn("Chỉ chọn những đề xuất Chưa phê duyệt!");
                        return false;
                    }
                }

                approvalItems.add(timekeepingSheetData);
            }
        }

        if (approvalItems.isEmpty()) {
            CommonFaces.showMessageWarn("Chưa chọn bản ghi nào!");
            return false;
        }


        if (newState.equals("1")) {
            changeStateMany("1");
        }

        return true;
    }

    public void changeStateMany(String newState) {
        List<TimekeepingSheetData> approvalItems = new ArrayList();
        List<Integer> approvalIds = new ArrayList();

        if (!checkValidSelectedManyRecord(newState, approvalItems)) {
            return;
        }

        approvalIds = approvalItems.stream().map(timekeepingSheetData -> timekeepingSheetData.getAutoid()).collect(Collectors.toList());

        ApproveTimeKeeping approveTimeKeeping = new ApproveTimeKeeping();
        approveTimeKeeping.setState(Integer.parseInt(newState));
        approveTimeKeeping.setApprovalIds(approvalIds);

        if (!approvalItems.isEmpty()) {
            if (approvalItems.get(0).getApprovedBy().equals(user.getPeopleId())) {
                approveTimeKeeping.setApprovedBy(user.getPeopleId());
                approveTimeKeeping.setApprovedAt(new Date());
            } else if (!StringUtils.isEmpty(approvalItems.get(0).getRoleLevel2())
                    && approvalItems.get(0).getRoleLevel2().equals(user.getRole().getName())) {
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

        //timeKeepingSheetClient.approve(approveTimeKeeping);
        TimekeepingExplanationDao.updateApprovalLevel2ByIds(approveTimeKeeping);
        TimekeepingExplanationDao.updateState(approveTimeKeeping);
        showApprovalMessage(newState);
        initTblTimeKeepkingExplanation();
    }

    public void approveMany(String buttonCode) {
        List<TimekeepingSheetData> listItemForDeny = new ArrayList();
        List<TimekeepingSheetData> listItemForApproveLevel1 = new ArrayList();
        List<TimekeepingSheetData> listItemForApproveLevel2 = new ArrayList();

        boolean selectedRecord = false;
        for (TimekeepingSheetData timekeepingSheetData : dataModel.getDatasource()) {
            if (ObjectUtils.isEmpty(timekeepingSheetData.getSelectedCheckbox())
                    || (!ObjectUtils.isEmpty(timekeepingSheetData.getSelectedCheckbox()) && !timekeepingSheetData.getSelectedCheckbox())) {
                continue;
            }
            selectedRecord = true;

            if (buttonCode.equals("DENY")) {
                if (timekeepingSheetData.getState().equals("0")) {
                    listItemForDeny.add(timekeepingSheetData);
                }
            } else if (buttonCode.equals("APPROVE")) {
                if (timekeepingSheetData.getState().equals("0") && timekeepingSheetData.getApprovedBy().equals(user.getPeopleId())
                        && timekeepingSheetData.getApprovalLevel().equals(2)) {
                    listItemForApproveLevel1.add(timekeepingSheetData);

                } if (timekeepingSheetData.getState().equals("0") && timekeepingSheetData.getApprovedBy().equals(user.getPeopleId())
                        && timekeepingSheetData.getApprovalLevel().equals(1)) {
                    listItemForApproveLevel2.add(timekeepingSheetData);

                } else if (timekeepingSheetData.getState().equals("1") && timekeepingSheetData.getApprovalLevel().equals(2)
                        && timekeepingSheetData.getRoleLevel2().equals(user.getRole().getName())) {
                    listItemForApproveLevel2.add(timekeepingSheetData);
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

        initTblTimeKeepkingExplanation();
    }

    public void updateStateMany(String newState, List<TimekeepingSheetData> listSelectedItems) {
        if (listSelectedItems.isEmpty()) {
            return;
        }

        List<Integer> approvalIds = new ArrayList();
        approvalIds = listSelectedItems.stream().map(timekeepingSheetData -> timekeepingSheetData.getAutoid()).collect(Collectors.toList());

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

        TimekeepingExplanationDao.updateApprovalLevel2ByIds(approveTimeKeeping);
        TimekeepingExplanationDao.updateState(approveTimeKeeping);

    }

    public void checkSelectedItems() {
        boolean selectedRecord = false;
        for (TimekeepingSheetData timekeepingSheetData : dataModel.getDatasource()) {
            if (ObjectUtils.isEmpty(timekeepingSheetData.getSelectedCheckbox())
                    || (!ObjectUtils.isEmpty(timekeepingSheetData.getSelectedCheckbox()) && !timekeepingSheetData.getSelectedCheckbox())) {
                continue;
            }
            selectedRecord = true;
        }

        if (!selectedRecord) {
            CommonFaces.showMessageWarn("Chưa chọn bản ghi nào!");
            return;
        }

        PrimeFaces current = PrimeFaces.current();
        current.executeScript("PF('dagConfirmDenyMany').show();");
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

    public boolean renderDenyButton() {
        if (Objects.isNull(updateTimeKeepingSheet)) {
            return false;
        }

        if (StringUtils.isEmpty(updateTimeKeepingSheet.getState())) {
            return false;
        }

        if (updateTimeKeepingSheet.getState().equals("2") || updateTimeKeepingSheet.getState().equals("3")) {
            return false;
        } else {
            return true;
        }
    }

    public boolean renderButton(String buttonCode) {
        if (Objects.isNull(updateTimeKeepingSheet)) {
            return false;
        }

        String buttonCodeFromMap = getApprovalFlowByGroupId.get(updateTimeKeepingSheet.getGroupId());

        if (buttonCode.equals(Constants.ApprovalFLow.NEXT_APPROVAL)) {
            if (!StringUtils.isEmpty(updateTimeKeepingSheet.getState()) && !updateTimeKeepingSheet.getState().equals("0")) {
                return false;
            }
        }

        if (buttonCode.equals(Constants.ApprovalFLow.APPROVAL)) {
            if (!StringUtils.isEmpty(updateTimeKeepingSheet.getState()) && (updateTimeKeepingSheet.getState().equals("2")
                    || updateTimeKeepingSheet.getState().equals("3"))) {
                return false;
            }
        }

        if (StringUtils.isEmpty(buttonCodeFromMap)) {
            return false;
        }

        if (buttonCode.equals(buttonCodeFromMap)) {
            return true;
        } else {
            return false;
        }
    }

    public boolean renderApproveManyButton(String buttonCode) {
        if (ObjectUtils.isEmpty(listApprovalFlow) || (!ObjectUtils.isEmpty(listApprovalFlow) && listApprovalFlow.isEmpty())) {
            return false;

        } else if (buttonCode.equals(Constants.ApprovalFLow.APPROVAL)) {
            if (listApprovalFlow.size() == 1) {
                ApprovalFlow approvalFlow = listApprovalFlow.get(0);

                if (approvalFlow.getApprovalLevel().equals(1)) {
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
            if (listApprovalFlow.size() == 1) {
                ApprovalFlow approvalFlow = listApprovalFlow.get(0);

                if (approvalFlow.getApprovalLevel().equals(1)) {
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