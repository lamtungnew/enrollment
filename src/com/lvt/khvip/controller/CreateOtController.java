/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.lvt.khvip.client.ApprovePeopleClient;
import com.lvt.khvip.client.OtSheetClient;
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
import javax.servlet.http.HttpSession;
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
public class CreateOtController implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(CreateOtController.class);

    private List<People> listPeopleForSearch;
    private List<People> listApprovePeople;
    private ApprovePeople approvePeople;
    private Map<String, People> peopleByFullName = new HashMap();
    private Map<String, People> peopleByPeopleId = new HashMap();

    private List<TimekeepingSheetExcel> listTimekeepingSheet;
    private List<Groups> listGroupForSearch;
    private Map<Integer, Groups> groupById;
    private OtSheetData otSheet = new OtSheetData();

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
    private OtSheetClient otSheetClient = new OtSheetClient();
    private ApprovePeopleClient approvePeopleClient = new ApprovePeopleClient();
    private OtSheetDataModel dataModel;
    private Date fromDate;
    private Date toDate;
    private String rootPath = ConfProperties.getProperty("root.path");
    private String imageFolder = ConfProperties.getProperty("image.folder");
    private String imageRootUrl = ConfProperties.getProperty("image.root.url");
    private OtSheetData updateOtSheet = new OtSheetData();
    private List<OtSheetDate> listUpdateOtSheetDate = new ArrayList();
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

    public void initUser() {
        user = (User) SessionUtils.getSession().getAttribute("user-info");
        userInfo = peopleByPeopleId.get(user.getPeopleId());
    }

    public void resetFilter() {
        if (!ObjectUtils.isEmpty(otSheet)) {
            otSheet.setCreatedAt(null);
            otSheet.setState(null);
            initTblOt();
        }
    }

    public void initListUpdateOtSheet(String action) {
        if (action.equals("ADD")) {
            OtSheetDate otSheetDate = new OtSheetDate();
            listUpdateOtSheetDate.add(otSheetDate);
        }
    }

    public void initTimeKeepingDate() {
        listTimekeepingDate = TimekeepingDateDao.getListTimeKeepingDate(null);
    }

    public CreateOtController() {
        groupsDao = new GroupsDao();
        initListGroupForSearch();
        initListPeopleForSearch();
        initUser();
        initApprovalFlow();
        initTblOt();
        initTimeKeepingDate();
        initListApprovePeople();
    }

    public void initListApprovePeople() {
        if (!ObjectUtils.isEmpty(approvalFlow)) {
            approvePeople = approvePeopleClient.getApprovePeople(user.getPeopleId(), user.getUsername(),
                    approvalFlow.getApprovalType(), 1);
            listApprovePeople = approvePeople.getPeopleIds();
        }
    }

    public void initApprovalFlow() {
        if (ObjectUtils.isEmpty(user)) {
            return;
        }

        approvalFlow = TimekeepingExplanationDao.getApprovalFlowByGroupId(userInfo.getGroupId(), Constants.ApprovalFLow.OT_APPROVAL);

        if (ObjectUtils.isEmpty(approvalFlow)) {
            return;
        }

        Integer groupId = 0;
        String role = "";

        if (approvalFlow.getApprovalLevel1().equals(Constants.RoleConstants.DEP_LEADER)) {
            groupId = approvalFlow.getGroupId();
            role = Constants.RoleConstants.DEP_LEADER;
        } else if (approvalFlow.getApprovalLevel1().equals(Constants.RoleConstants.GROUP_LEADER)) {
            groupId = approvalFlow.getDepId();
            role = Constants.RoleConstants.GROUP_LEADER;
        } else if (approvalFlow.getApprovalLevel1().equals(Constants.RoleConstants.ADMIN)) {
            role = Constants.RoleConstants.ADMIN;
        }

        User approvalPeopleLevel1 = PeopleDao.getApprovalNameByGroupId(groupId, role);
        if (!ObjectUtils.isEmpty(approvalPeopleLevel1)) {
            approvalFlow.setApprovalLevel1PeopleId(approvalPeopleLevel1.getPeopleId());
            approvalFlow.setApprovalLevel1Name(approvalPeopleLevel1.getUsername());
        }

        if (approvalFlow.getApprovalLevel().equals(2)) {
            Integer groupIdLevel2 = 0;
            String roleLevel2 = "";

            if (approvalFlow.getApprovalLevel2().equals(Constants.RoleConstants.DEP_LEADER)) {
                groupIdLevel2 = approvalFlow.getGroupId();
                roleLevel2 = Constants.RoleConstants.DEP_LEADER;
            } else if (approvalFlow.getApprovalLevel2().equals(Constants.RoleConstants.GROUP_LEADER)) {
                groupIdLevel2 = approvalFlow.getDepId();
                roleLevel2 = Constants.RoleConstants.GROUP_LEADER;
            } else if (approvalFlow.getApprovalLevel2().equals(Constants.RoleConstants.ADMIN)) {
                roleLevel2 = Constants.RoleConstants.ADMIN;
            }

            User approvalPeopleLevel2 = PeopleDao.getApprovalNameByGroupId(groupIdLevel2, roleLevel2);

            if (!ObjectUtils.isEmpty(approvalPeopleLevel2)) {
                approvalFlow.setApprovalLevel2PeopleId(approvalPeopleLevel2.getPeopleId());
                approvalFlow.setApprovalLevel2Name(approvalPeopleLevel2.getUsername());
            }
        }
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

    public void closeDlgUploadStatus() {
        reset();
    }

    public void closeDlgAddNewOtSheet() {
        updateOtSheet = new OtSheetData();
        listUpdateOtSheetDate.clear();
    }

    public void changeState(String newState) {
        OtSheetData updateItem = new OtSheetData();
        updateItem.setAutoid(updateOtSheet.getAutoid());
        updateItem.setState(newState);
        otSheetClient.updateOtSheet(updateItem);
        initTblOt();
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

    public void initSelectedOtSheet(OtSheetData OtSheetData) {
        this.updateOtSheet = OtSheetData;

        String orgName = "";
        Groups org = groupById.get(updateOtSheet.getOrgId());
        if (!ObjectUtils.isEmpty(org)) {
            orgName = org.getGroupName();
        }
        updateOtSheet.setOrgName(orgName);

        listUpdateOtSheetDate.addAll(updateOtSheet.getOtSheetDates());

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/YYYY");
        for (OtSheetDate otSheetDate : listUpdateOtSheetDate) {
            if (StringUtils.isEmpty(otSheetDate.getOtDate())) {
                continue;
            }

            try {
                otSheetDate.setOtDateTypeDate(simpleDateFormat.parse(otSheetDate.getOtDate()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    public void initAddNewTimeKeepingSheet() {
        if (ObjectUtils.isEmpty(user) || StringUtils.isEmpty(user.getPeopleId())) {
            return;
        }

        People people = peopleByPeopleId.get(user.getPeopleId());

        if (!ObjectUtils.isEmpty(people)) {
            updateOtSheet.setFullName(people.getFullName());
            updateOtSheet.setPeopleId(people.getPeopleId());
            updateOtSheet.setGroupId(people.getGroupId());
            updateOtSheet.setGroupName(people.getGroupName());

            if (StringUtils.isEmpty(updateOtSheet.getApprovedBy()) && !ObjectUtils.isEmpty(listApprovePeople)
                    && listApprovePeople.size() == 1) {
                updateOtSheet.setApprovedBy(listApprovePeople.get(0).getPeopleId());
            }
        }
    }

    public void initTblOt() {
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

        otSheet.setFromDate(convertedFromDate);
        otSheet.setToDate(convertedToDate);

        if (!ObjectUtils.isEmpty(user) && !StringUtils.isEmpty(user.getPeopleId())) {
            otSheet.setPeopleId(user.getPeopleId());
            dataModel = new OtSheetDataModel("tblListApprove", otSheet, otSheetClient);
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
        groupById = listGroupForSearch.stream().
                collect(Collectors.toMap(groupItem -> groupItem.getGroupId(), Function.identity(),
                        (existing, replacement) -> existing));
    }

    public void search() {
        initTblOt();
    }

    public void createOrUpdateOtSheet(String action) {
        if (ObjectUtils.isEmpty(listApprovePeople)) {
            CommonFaces.showMessageWarn("Phòng này chưa cài đặt thông tin người duyệt!");
            return;
        }

        PrimeFaces current = PrimeFaces.current();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/YYYY");

        if (action.equals("ADD")) {
            for (OtSheetDate otSheetDate : listUpdateOtSheetDate) {
                otSheetDate.setOtDate(simpleDateFormat.format(otSheetDate.getOtDateTypeDate()));
            }

            updateOtSheet.setOtSheetDates(listUpdateOtSheetDate);
            updateOtSheet.setProject("");
            updateOtSheet.setState("0");
            updateOtSheet.setRuleId(approvePeople.getRuleId());

            if (!ObjectUtils.isEmpty(approvalFlow)) {
                if (!ObjectUtils.isEmpty(approvalFlow.getApprovalLevel())) {
                    updateOtSheet.setApprovalLevel(approvalFlow.getApprovalLevel());
                }

                if (!ObjectUtils.isEmpty(approvalFlow.getApprovalLevel1())) {
                    updateOtSheet.setRoleLevel1(approvalFlow.getApprovalLevel1());
                }

                if (!ObjectUtils.isEmpty(approvalFlow.getApprovalLevel2())) {
                    updateOtSheet.setRoleLevel2(approvalFlow.getApprovalLevel2());
                }
            }

            otSheetClient.addNewOtSheet(updateOtSheet);

            current.executeScript("PF('dagAddOt').hide();");
            CommonFaces.showGrowlInfo("Thêm đề xuất thành công!");
        } else if (action.equals("EDIT")) {
            updateOtSheet.setOtSheetDates(listUpdateOtSheetDate);
            updateOtSheet.setProject("");
            otSheetClient.editTimeKeepingSheet(updateOtSheet);

            current.executeScript("PF('dagEditTimeKeeping').hide();");
            CommonFaces.showGrowlInfo("Sửa đề xuất thành công!");
        }

        initTblOt();
    }

    public void deleteListUpdateOtSheetItem(OtSheetData item) {
        Iterator itr = listUpdateOtSheetDate.iterator();

        while (itr.hasNext()) {
            OtSheetData searchItem = (OtSheetData) itr.next();
            if (searchItem.equals(item))
                itr.remove();
        }
    }

    public void grantDeleteObject(OtSheetData deleteObj) {
        this.updateOtSheet = deleteObj;
    }

    public void delete() {
        OtSheetDao.deleteOt(updateOtSheet.getAutoid());
        CommonFaces.showGrowlInfo("Xóa đề xuất thành công!");
        initTblOt();
    }
}
