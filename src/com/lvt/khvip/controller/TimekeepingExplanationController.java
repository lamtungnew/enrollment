/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.lvt.khvip.client.TimeKeepingSheetClient;
import com.lvt.khvip.client.dto.TimekeepingSheetData;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.dao.*;
import com.lvt.khvip.entity.*;
import com.lvt.khvip.model.TimeKeepingSheetDataModel;
import com.lvt.khvip.util.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.beanutils.BeanUtils;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
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
public class TimekeepingExplanationController implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(TimekeepingExplanationController.class);

    private List<People> listPeopleForSearch;
    private List<String> listApprovePeople;
    private Map<String, People> peopleByFullName = new HashMap();
    private Map<String, People> peopleByPeopleId = new HashMap();

    private List<TimekeepingSheetExcel> listTimekeepingSheet;
    private List<Groups> listGroupForSearch;

    private TimekeepingSheetData searchTimekeepingSheet = new TimekeepingSheetData();

    private GroupsDao groupsDao;
    private ExcelUtils excelUtils = new ExcelUtils();
    private UploadedFile uploadedExcel;
    private UploadedFile uploadedImage;
    private UploadedFile signedFile;
    private String uploadExcelStatus;
    private String errorSummary;
    private byte[] attachedErrorFileByteArr;
    private StreamedContent content;
    private StreamedContent exportExcelContent;
    private String title;
    private boolean disableErrorLink = true;
    private TimeKeepingSheetClient timeKeepingSheetClient = new TimeKeepingSheetClient();
    private TimeKeepingSheetDataModel dataModel;
    private Date fromDate;
    private Date toDate;
    private String rootPath = ConfProperties.getProperty("root.path");
    private String imageFolder = ConfProperties.getProperty("image.folder");
    private String imageRootUrl = ConfProperties.getProperty("image.root.url");
    private TimekeepingSheetData updateTimeKeepingSheet = new TimekeepingSheetData();
    private List<TimekeepingSheetData> listUpdateTimeKeepingSheet = new ArrayList();
    private List<TimekeepingDate> listTimekeepingDate = new ArrayList();
    private String noRecordMsg = "không có dữ liệu...";
    private Map<Integer, String> getApprovalFlowByGroupId = new HashMap();
    private List<String> approvalList = new ArrayList();
    private User user;
    private String selectedApproval = "";
    private List<ApprovalFlow> listApprovalFlow;

    public void reset() {
        uploadExcelStatus = "";
        errorSummary = "";
        content = null;
        disableErrorLink = true;
        title = "";
        uploadedExcel = null;
    }

    public void resetFilter() {
        if (!ObjectUtils.isEmpty(searchTimekeepingSheet)) {
            searchTimekeepingSheet.setCreatedAt(null);
            searchTimekeepingSheet.setState(null);
            initTblTimeKeepkingExplanation();
        }
    }

    public void initListUpdateTimeKeepingSheet(String action) {
        if (action.equals("ADD")) {
            TimekeepingSheetData timekeepingSheet = new TimekeepingSheetData();
            listUpdateTimeKeepingSheet.add(timekeepingSheet);
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
                    TimekeepingExplanationDao.getApprovalFlowByRole(user.getRole().getName(),
                            depId, Constants.ApprovalFLow.KEEPING_APPROVAL);

            for (ApprovalFlow approvalFlow : listApprovalFlow) {
                //getApprovalFlowByGroupId.put(approvalFlow.getGroupId(),approvalFlow);

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

    public void initUser() {
        user = (User) SessionUtils.getSession().getAttribute("user-info");

        if (!ObjectUtils.isEmpty(user) && !StringUtils.isEmpty(user.getPeopleId())) {
            People people = peopleByPeopleId.get(user.getPeopleId());

            if (!ObjectUtils.isEmpty(people) && !ObjectUtils.isEmpty(people.getFullName())) {
                user.setFullName(people.getFullName());
            }
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

    public TimekeepingExplanationController() {
        groupsDao = new GroupsDao();
        initUser();
        initListApprovalFlow();
        initTblTimeKeepkingExplanation();
        initListPeopleForSearch();
        initListGroupForSearch();
        initTimeKeepingDate();
        checkRooth();
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
            ExcelTemplate excelTemplate = excelUtils.getExcelTemplate(Constants.ExcelTemplateCodes.TIMEKEEPINGSHEET_UPLOAD);
            ExcelResponseMessage excelResponseMessage = new ExcelResponseMessage();
            String fileName = uploadedExcel.getFileName() != null ? uploadedExcel.getFileName() : "";

            if (!excelUtils.validFileType(fileName, excelResponseMessage, errorSummarySet)) {
                disableErrorLink = false;
                showResultDialog(errorSummarySet);
                return;
            }

            Workbook workbook = excelUtils.initWorkbook(byteArr, fileName);

            Sheet sheet = workbook.getSheetAt(excelTemplate.getSheetSeqNo());

            if (excelUtils.checkEmptyFile(sheet, excelTemplate.getStartRow(), excelResponseMessage, errorSummarySet)) {
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

    public void validateAndSaveExcelData(Workbook workbook, Sheet sheet, String fileName, Map<Integer,
            StringBuilder> mapExcelError, Set<String> errorSummarySet, ExcelTemplate excelTemplate) {
        List<TimekeepingSheetExcel> listTimeKeepingSheet = excelUtils.parseExcelToDto(TimekeepingSheetExcel.class, sheet, fileName, excelTemplate, mapExcelError, errorSummarySet);

        checkValidState(listTimeKeepingSheet, mapExcelError, errorSummarySet);
        checkValidPeopleId(listTimeKeepingSheet, mapExcelError, errorSummarySet);
        checkValidPeopleFullName(listTimeKeepingSheet, mapExcelError, errorSummarySet);
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
                initTblTimeKeepkingExplanation();
            }
        }
    }

    public void checkValidPeopleId(List<TimekeepingSheetExcel> listTimeKeepingSheet, Map<Integer, StringBuilder> mapExcelError, Set<String> errorSummarySet) {
        for (int itemIndex = 0; itemIndex < listTimeKeepingSheet.size(); itemIndex++) {
            TimekeepingSheetExcel timekeepingSheetExcel = listTimeKeepingSheet.get(itemIndex);

            if (StringUtils.isEmpty(timekeepingSheetExcel.getPeopleId())) {
                continue;
            }

            People condition = new People();
            condition.setPeopleId(timekeepingSheetExcel.getPeopleId());
            int countPeopleId = PeopleDao.countPeopleByCondition(condition);

            if (countPeopleId == 0) {
                StringBuilder cellErrors = mapExcelError.get(itemIndex);
                if (cellErrors == null) {
                    cellErrors = new StringBuilder();
                }

                excelUtils.buildCellErrors(cellErrors,
                        MessageProvider.getValue("upload.excel.error.data.not.existing",
                                new String[]{Constants.TimeKeepingSheetConstants.PEOPLE_ID}));

                errorSummarySet.add(MessageProvider.getValue("upload.excel.email.invalid.entries", null));
                mapExcelError.put(itemIndex, cellErrors);
            }
        }
    }

//	public void checkValidState(List<TimekeepingSheetExcel> listTimeKeepingSheet, Map<Integer,StringBuilder> mapExcelError, Set<String> errorSummarySet) {
//		for (int itemIndex =0 ;itemIndex<listTimeKeepingSheet.size(); itemIndex++) {
//			TimekeepingSheetExcel timekeepingSheetExcel = listTimeKeepingSheet.get(itemIndex);
//
//			if (StringUtils.isEmpty(timekeepingSheetExcel.getState())){
//				continue;
//			}
//
//			if (!timekeepingSheetExcel.getState().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.APPROVED_STATUS_FOR_COMPARE)
//			&& !timekeepingSheetExcel.getState().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.DENY_STATUS_FOR_COMPARE)
//			&& !timekeepingSheetExcel.getState().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.NEW_STATUS_FOR_COMPARE)) {
//				StringBuilder cellErrors = mapExcelError.get(itemIndex);
//				if (cellErrors == null) {
//					cellErrors = new StringBuilder();
//				}
//
//				excelUtils.buildCellErrors(cellErrors,
//						MessageProvider.getValue("upload.excel.error.data.invalid.option",
//								new String[] { "Trạng thái", Constants.TimeKeepingSheetConstants}));
//
//				errorSummarySet.add(MessageProvider.getValue("upload.excel.email.invalid.entries", null));
//				mapExcelError.put(itemIndex, cellErrors);
//			}
//		}
//	}

    public void checkValidPeopleFullName(List<TimekeepingSheetExcel> listTimeKeepingSheet, Map<Integer, StringBuilder> mapExcelError, Set<String> errorSummarySet) {
        for (int itemIndex = 0; itemIndex < listTimeKeepingSheet.size(); itemIndex++) {
            TimekeepingSheetExcel timekeepingSheetExcel = listTimeKeepingSheet.get(itemIndex);

            if (StringUtils.isEmpty(timekeepingSheetExcel.getFullName()) || !StringUtils.isEmpty(timekeepingSheetExcel.getPeopleId())) {
                continue;
            }

            List<String> listPeopleId = PeopleDao.getListPeopleIdByName(timekeepingSheetExcel.getFullName().trim().toLowerCase());

            if (listPeopleId.size() == 1) {
                timekeepingSheetExcel.setPeopleId(listPeopleId.get(0));
            } else if (listPeopleId.size() > 1) {
                StringBuilder cellErrors = mapExcelError.get(itemIndex);
                if (cellErrors == null) {
                    cellErrors = new StringBuilder();
                }

                excelUtils.buildCellErrors(cellErrors,
                        MessageProvider.getValue("upload.excel.error.data.more.than.one",
                                new String[]{Constants.TimeKeepingSheetConstants.FULL_NAME, timekeepingSheetExcel.getFullName()}));

                errorSummarySet.add(MessageProvider.getValue("upload.excel.data.found.more.than.1", null));
                mapExcelError.put(itemIndex, cellErrors);
            } else if (listPeopleId.size() == 0) {
                StringBuilder cellErrors = mapExcelError.get(itemIndex);
                if (cellErrors == null) {
                    cellErrors = new StringBuilder();
                }

                excelUtils.buildCellErrors(cellErrors,
                        MessageProvider.getValue("upload.excel.error.data.not.existing", null));

                errorSummarySet.add(MessageProvider.getValue("upload.excel.email.invalid.entries", null));
                mapExcelError.put(itemIndex, cellErrors);
            }
        }
    }

    public void setAutoIdForExcelItem(List<TimekeepingSheetExcel> listTimeKeepingSheet, Map<Integer, StringBuilder> mapExcelError, Set<String> errorSummarySet) {
        TimekeepingSheet condition = new TimekeepingSheet();
        StringBuilder peopleIdAndKeepingDate = new StringBuilder();

        for (TimekeepingSheetExcel timekeepingSheetExcel : listTimeKeepingSheet) {
            if (StringUtils.isEmpty(peopleIdAndKeepingDate.toString())) {
                peopleIdAndKeepingDate.append(timekeepingSheetExcel.getPeopleId() + "||" + timekeepingSheetExcel.getKeepingDate());
            } else {
                peopleIdAndKeepingDate.append("," + timekeepingSheetExcel.getPeopleId() + "||" + timekeepingSheetExcel.getKeepingDate());
            }
        }

        condition.setPeopleIdAndKeepingDate(peopleIdAndKeepingDate.toString());

        List<TimekeepingSheetExcel> listTimeKeepingSheetForSettingAutoId = TimekeepingExplanationDao.getListTimeKeepingSheet(condition);
        Map<String, Integer> autoIdByPeopleIdAndKeepingDate = listTimeKeepingSheetForSettingAutoId.stream().collect
                (Collectors.toMap(timekeepingSheetExcel -> timekeepingSheetExcel.getPeopleId() + "||" + timekeepingSheetExcel.getKeepingDate(),
                        TimekeepingSheetExcel::getAutoid, (existing, replacement) -> existing));


        for (int itemIndex = 0; itemIndex < listTimeKeepingSheet.size(); itemIndex++) {
            TimekeepingSheetExcel timekeepingSheetExcel = listTimeKeepingSheet.get(itemIndex);

            if (StringUtils.isEmpty(timekeepingSheetExcel.getPeopleId()) || StringUtils.isEmpty(timekeepingSheetExcel.getKeepingDateString())) {
                continue;
            }

            timekeepingSheetExcel.setPeopleIdAndKeepingDate(timekeepingSheetExcel.getPeopleId() + "||" + timekeepingSheetExcel.getKeepingDateString());

            timekeepingSheetExcel.setAutoid(autoIdByPeopleIdAndKeepingDate.get(timekeepingSheetExcel.getPeopleIdAndKeepingDate()));

            if (ObjectUtils.isEmpty(timekeepingSheetExcel.getAutoid())) {
                StringBuilder cellErrors = mapExcelError.get(itemIndex);
                if (cellErrors == null) {
                    cellErrors = new StringBuilder();
                }

                excelUtils.buildCellErrors(cellErrors,
                        MessageProvider.getValue("upload.excel.email.invalid.entries",
                                new String[]{Constants.TimeKeepingSheetConstants.FULL_NAME, timekeepingSheetExcel.getFullName()}));

                errorSummarySet.add(MessageProvider.getValue("upload.excel.error.peopleId.or.keepingDate.not.existing", null));
                mapExcelError.put(itemIndex, cellErrors);
            }
        }
    }


    public void checkValidState(List<TimekeepingSheetExcel> listTimeKeepingSheet, Map<Integer, StringBuilder> mapExcelError, Set<String> errorSummarySet) {
        for (int itemIndex = 0; itemIndex < listTimeKeepingSheet.size(); itemIndex++) {
            TimekeepingSheetExcel timekeepingSheetExcel = listTimeKeepingSheet.get(itemIndex);

            if (timekeepingSheetExcel.getState().toLowerCase().contains(Constants.TimeKeepingSheetConstants.NEW_STATUS_FOR_COMPARE)) {
                timekeepingSheetExcel.setState("0");
            } else if (timekeepingSheetExcel.getState().toLowerCase().contains(Constants.TimeKeepingSheetConstants.NEXT_APPROVED_STATUS_FOR_COMPARE)) {
                timekeepingSheetExcel.setState("1");
            } else if (timekeepingSheetExcel.getState().toLowerCase().contains(Constants.TimeKeepingSheetConstants.APPROVED_STATUS_FOR_COMPARE)) {
                timekeepingSheetExcel.setState("2");
            } else if (timekeepingSheetExcel.getState().toLowerCase().contains(Constants.TimeKeepingSheetConstants.DENY_STATUS_FOR_COMPARE)) {
                timekeepingSheetExcel.setState("3");
            } else {
                StringBuilder cellErrors = mapExcelError.get(itemIndex);
                if (cellErrors == null) {
                    cellErrors = new StringBuilder();
                }
                excelUtils.buildCellErrors(cellErrors,
                        MessageProvider.getValue("upload.excel.error.data.invalid.option",
                                new String[]{Constants.TimeKeepingSheetConstants.STATE,
                                        Constants.TimeKeepingSheetConstants.VALID_STATE}));
                errorSummarySet.add(MessageProvider.getValue("upload.excel.email.invalid.entries", null));
                mapExcelError.put(itemIndex, cellErrors);
            }
        }
    }

    public void exportExcel() {
        if (ObjectUtils.isEmpty(dataModel.getDatasource())) {
            return;
        }

        ExcelTemplate excelTemplate = excelUtils.getExcelTemplate(Constants.ExcelTemplateCodes.TIMEKEEPINGSHEET_EXPORT);

        List<TimekeepingSheetExcel> listDataForExport = new ArrayList();

        for (TimekeepingSheetData timekeepingSheetData : dataModel.getDatasource()) {
            try {
                TimekeepingSheetExcel timekeepingSheetExcel = new TimekeepingSheetExcel();
                BeanUtils.copyProperties(timekeepingSheetExcel, timekeepingSheetData);
                timekeepingSheetExcel.setCustomerType(timekeepingSheetData.getCusType());
                listDataForExport.add(timekeepingSheetExcel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        byte[] exportDataByteArr = excelUtils.exportExcel(listDataForExport, TimekeepingSheetExcel.class, excelTemplate);

        String fileName = "time_checking.xlsx";
        InputStream downloadFileInputStream = new ByteArrayInputStream(exportDataByteArr);
        exportExcelContent = DefaultStreamedContent.builder()
                .name(fileName)
                .stream(() -> downloadFileInputStream).build();

    }

    public void saveExcelData(List<TimekeepingSheetExcel> listTimeKeepingSheet, Map<Integer,
            StringBuilder> mapExcelError, Set<String> errorSummarySet) {

        listTimeKeepingSheet.stream().forEach(timeKeepingSheet -> {
//			timeKeepingSheet.setCreatedAt(new Date());
//			timeKeepingSheet.setApprovedBy(SessionUtils.getUserName());
//			timeKeepingSheet.setCreatedBy(SessionUtils.getUserName());
            timeKeepingSheet.setDateType("1");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");

            if (!StringUtils.isEmpty(timeKeepingSheet.getState())) {
                if (timeKeepingSheet.getState().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.APPROVED_STATUS_FOR_COMPARE)) {
                    timeKeepingSheet.setState("1");

                } else if (timeKeepingSheet.getState().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.DENY_STATUS_FOR_COMPARE)) {
                    timeKeepingSheet.setState("2");

                } else if (timeKeepingSheet.getState().trim().equalsIgnoreCase(Constants.TimeKeepingSheetConstants.NEW_STATUS_FOR_COMPARE)) {
                    timeKeepingSheet.setState("0");

                }
            }

            if (!ObjectUtils.isEmpty(timeKeepingSheet.getKeepingDateString())) {
                try {
                    timeKeepingSheet.setKeepingDate(simpleDateFormat.parse(timeKeepingSheet.getKeepingDateString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

//			TimekeepingSheetData timeKeepingSheetData = new TimekeepingSheetData();
//			try {
//				BeanUtils.copyProperties(timeKeepingSheet, timeKeepingSheetData);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//
            //timeKeepingSheetClient.updateTimeKeepingSheet(timeKeepingSheetData);
            //TimekeepingExplanationDao.updateStateOneRecord(timeKeepingSheet.getAutoid(),timeKeepingSheet.getState());

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
        ImportExcelDto<TimekeepingSheetExcel> importResult = timeKeepingSheetClient.importExcel(importExcelDto);

        Map<String, TimekeepingSheetExcel> mListErrorAfter = new HashMap<>();

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
                TimekeepingSheetExcel item = listTimeKeepingSheet.get(i);
                String key = item.getPeopleId() + "_" + item.getGroupId();
                if (mListErrorAfter.containsKey(key)) {
                    TimekeepingSheetExcel itemError = mListErrorAfter.get(key);
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
        String filePath = rootPath + imageFolder + fileName;
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
                    + "time_checking_error_" + new Date().getTime() + ".xlsx";
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

    public void initSelectedTimeKeepingSheet(TimekeepingSheetData timeKeepingSheetData) {
        this.updateTimeKeepingSheet = timeKeepingSheetData;
        TimekeepingSheetData condition = new TimekeepingSheetData();
        condition.setAutoid(updateTimeKeepingSheet.getAutoid());
        TimekeepingSheetData timeKeepingSheet = timeKeepingSheetClient.getTimeKeepingSheet(condition);

        updateTimeKeepingSheet.setTimeKeepingDate(timeKeepingSheet.getTimeKeepingDate());
        updateTimeKeepingSheet.setSupervisor(timeKeepingSheet.getSupervisor());

        if (!ObjectUtils.isEmpty(updateTimeKeepingSheet.getTimeKeepingDate())) {
            List<String> listTimeKeepingDate = new ArrayList<>();
            listTimeKeepingDate = updateTimeKeepingSheet.getTimeKeepingDate().stream()
                    .map(timekeepingDate -> timekeepingDate.getDate()).collect(Collectors.toList());
            updateTimeKeepingSheet.setTimeKeepingDateCbxValues(listTimeKeepingDate.toArray(new String[0]));
        }

        if (!ObjectUtils.isEmpty(updateTimeKeepingSheet.getGroupId())) {
            listApprovePeople = timeKeepingSheetClient.getListManager(updateTimeKeepingSheet.getGroupId());
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
//        String keepingDateForSearch = "";
//        if (!ObjectUtils.isEmpty(convertedFromDate) && !ObjectUtils.isEmpty(convertedToDate)) {
//            keepingDateForSearch = "&keepingDate=gte:" + convertedFromDate.getTime() + "%20and%20lte:" + convertedToDate.getTime();
//        } else if (!ObjectUtils.isEmpty(convertedFromDate) && ObjectUtils.isEmpty(convertedToDate)) {
//            keepingDateForSearch = "&keepingDate=gte:" + convertedFromDate.getTime();
//        } else if (ObjectUtils.isEmpty(convertedFromDate) && !ObjectUtils.isEmpty(convertedToDate)) {
//            keepingDateForSearch = "&keepingDate=lte:" + convertedToDate.getTime();
//        }

//        if (!StringUtils.isEmpty(keepingDateForSearch)) {
//            searchTimekeepingSheet.setKeepingDateForSearch(keepingDateForSearch);
//        } else {
//            searchTimekeepingSheet.setKeepingDateForSearch("");
//        }

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

    public void createOrUpdateTimeKeepingSheet(String action) {
        PrimeFaces current = PrimeFaces.current();

        if (action.equals("ADD")) {
            for (TimekeepingSheetData timeKeepingSheetData : listUpdateTimeKeepingSheet) {
                timeKeepingSheetData.setState("0");

                StringBuilder imagePath = new StringBuilder();
                StringBuilder imageUrl = new StringBuilder();
                handleUploadImage(imagePath, imageUrl, timeKeepingSheetData.getUploadedImage());

                timeKeepingSheetData.setImagePath(imagePath.toString());
                timeKeepingSheetData.setTimeKeepingImage(imageUrl.toString());
                timeKeepingSheetData.setPeopleId(updateTimeKeepingSheet.getPeopleId());
                timeKeepingSheetData.setApprovedBy(updateTimeKeepingSheet.getApprovedBy());

                if (!ObjectUtils.isEmpty(timeKeepingSheetData.getTimeKeepingDateCbxValues())) {
                    for (String value : timeKeepingSheetData.getTimeKeepingDateCbxValues()) {
                        TimekeepingDate timekeepingDate = new TimekeepingDate();
                        timekeepingDate.setDate(value);
                        timeKeepingSheetData.getTimeKeepingDate().add(timekeepingDate);
                    }
                }

                timeKeepingSheetClient.addNewTimeKeepingSheet(timeKeepingSheetData);
            }

            current.executeScript("PF('dagAddTimeKeeping').hide();");
        } else if (action.equals("EDIT")) {
            TimekeepingSheetData timeKeepingSheetData = listUpdateTimeKeepingSheet.get(0);

            if (!ObjectUtils.isEmpty(timeKeepingSheetData.getUploadedImage())) {
                StringBuilder imagePath = new StringBuilder();
                StringBuilder imageUrl = new StringBuilder();
                handleUploadImage(imagePath, imageUrl, timeKeepingSheetData.getUploadedImage());
                timeKeepingSheetData.setImagePath(imagePath.toString());
                timeKeepingSheetData.setTimeKeepingImage(imageUrl.toString());
            }

            timeKeepingSheetData.setPeopleId(updateTimeKeepingSheet.getPeopleId());
            timeKeepingSheetData.setApprovedBy(updateTimeKeepingSheet.getApprovedBy());

            if (!ObjectUtils.isEmpty(timeKeepingSheetData.getTimeKeepingDateCbxValues())) {
                for (String value : timeKeepingSheetData.getTimeKeepingDateCbxValues()) {
                    TimekeepingDate timekeepingDate = new TimekeepingDate();
                    timekeepingDate.setDate(value);
                    timeKeepingSheetData.getTimeKeepingDate().add(timekeepingDate);
                }
            }

            timeKeepingSheetClient.editTimeKeepingSheet(timeKeepingSheetData);

            current.executeScript("PF('dagEditTimeKeeping').hide();");
        }

        initTblTimeKeepkingExplanation();
    }

    public void onChangeFullName(AjaxBehaviorEvent event) {
        if (ObjectUtils.isEmpty(((UIOutput) event.getSource()).getValue())) {
            updateTimeKeepingSheet.setPeopleId(null);
            updateTimeKeepingSheet.setGroupName(null);
            return;
        }

        String fullName = ((UIOutput) event.getSource()).getValue().toString();

        People people = peopleByFullName.get(fullName);

        if (!ObjectUtils.isEmpty(people)) {
            updateTimeKeepingSheet.setPeopleId(people.getPeopleId());
            updateTimeKeepingSheet.setGroupId(people.getGroupId());
            updateTimeKeepingSheet.setGroupName(people.getGroupName());
        }

        if (!ObjectUtils.isEmpty(updateTimeKeepingSheet.getGroupId())) {
            listApprovePeople = timeKeepingSheetClient.getListManager(updateTimeKeepingSheet.getGroupId());
        }
    }

    public void onChangePeopleId(AjaxBehaviorEvent event) {
        if (ObjectUtils.isEmpty(((UIOutput) event.getSource()).getValue())) {
            updateTimeKeepingSheet.setFullName(null);
            updateTimeKeepingSheet.setGroupName(null);
            return;
        }

        String peopleId = ((UIOutput) event.getSource()).getValue().toString();

        People people = peopleByPeopleId.get(peopleId);

        if (!ObjectUtils.isEmpty(people)) {
            updateTimeKeepingSheet.setFullName(people.getFullName());
            updateTimeKeepingSheet.setGroupId(people.getGroupId());
            updateTimeKeepingSheet.setGroupName(people.getGroupName());
        }

        if (!ObjectUtils.isEmpty(updateTimeKeepingSheet.getGroupId())) {
            listApprovePeople = timeKeepingSheetClient.getListManager(updateTimeKeepingSheet.getGroupId());
        }
    }

    public void deleteListUpdateTimeKeepingSheetItem(TimekeepingSheetData item) {
        Iterator itr = listUpdateTimeKeepingSheet.iterator();

        while (itr.hasNext()) {
            TimekeepingSheetData searchItem = (TimekeepingSheetData) itr.next();
            if (searchItem.equals(item))
                itr.remove();
        }
    }
}
