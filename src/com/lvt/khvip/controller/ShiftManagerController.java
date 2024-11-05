/**
 *
 */
package com.lvt.khvip.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.EmployeeClient;
import com.lvt.khvip.client.GroupUserClient;
import com.lvt.khvip.client.ShiftClient;
import com.lvt.khvip.client.ShiftConfigClient;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.entity.ExcelResponseMessage;
import com.lvt.khvip.entity.ExcelTemplate;
import com.lvt.khvip.model.ShiftDataModel;
import com.lvt.khvip.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.modelmapper.ModelMapper;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.ServletContext;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.*;

@ManagedBean
@SessionScoped
@Getter
@Setter
@Slf4j
public class ShiftManagerController implements Serializable {
    private ShiftClient shiftClient;
    private ShiftConfigClient shiftConfigClient;
    private GroupUserClient groupUserClient;
    private EmployeeClient employeeClient;
    private ShiftDataModel dataModel;
    private ShiftListData searchData;
    private ShiftListData selectedItem;
    private boolean updateMode;
    private boolean createMode;

    private List<GroupCatg> lstGroupCatg;
    private List<GroupCatg> lstGroupCatgChild;
    private List<ShiftCatg> lstShiftCatgAll;
    private List<ShiftCatg> lstShiftCatg;
    private List<Employee> lstPeople;

    private ShiftPeopleDetail shiftPeopleDetail;
    private ShiftConfigDto shiftPeopleDetailSelected;
    private LinkedHashMap<String, ShiftConfigDto> mShiftCreate = new LinkedHashMap<>();

    private String importTitle;
    private String errorSummary;
    private String uploadExcelStatus;
    private UploadedFile uploadedExcel;
    private UploadedFile uploadedImage;
    private boolean disableErrorLink = true;
    private byte[] attachedErrorFileByteArr;
    private StreamedContent content;
    private StreamedContent exportExcelContent;
    private StreamedContent importFileExamplecontent;
    private ExcelUtils excelUtils = new ExcelUtils();

    private String rootPath = ConfProperties.getProperty("root.path");
    private String imageFolder = ConfProperties.getProperty("image.folder");
    private String imageRootUrl = ConfProperties.getProperty("image.root.url");

    public static void main(String[] a) {
        try {
            List<ShiftConfigDto> lst = new ArrayList<>();
            lst.add(ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("01/01/2021"))
                    .expireDate(Utils.convertStringToDate("30/01/2021")).build());

            lst.add(ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("01/02/2021"))
                    .expireDate(Utils.convertStringToDate("28/02/2021")).build());

            lst.add(ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("01/04/2021"))
                    .expireDate(Utils.convertStringToDate("28/04/2021")).build());

            ShiftConfigDto in = ShiftConfigDto.builder()
                    .startDate(Utils.convertStringToDate("01/03/2021"))
                    .expireDate(Utils.convertStringToDate("01/04/2021")).build();

            for (ShiftConfigDto item : lst) {
                boolean betweenStart = betweenEq(in.getStartDate(), item.getStartDate(), item.getExpireDate());
                boolean betweenEnd = betweenEq(in.getExpireDate(), item.getStartDate(), item.getExpireDate());
                log.info(" >>> {} >= {} <= {} betweenStart {}, betweenEnd {}, rs: {}", in.getStartDate(), item.getStartDate(), item.getExpireDate(),
                        betweenStart, betweenEnd, (!betweenStart && !betweenEnd));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean between(Date date, Date dateStart, Date dateEnd) {
        if (date != null && dateStart != null && dateEnd != null) {
            if (date.after(dateStart) && date.before(dateEnd)) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean betweenEq(Date date, Date dateStart, Date dateEnd) {
        if (date != null && dateStart != null && dateEnd != null) {
            if ((date.compareTo(dateStart) == 0 || date.after(dateStart)) && (date.compareTo(dateStart) == 0 || date.before(dateEnd))) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    @PostConstruct
    public void init() {
        groupUserClient = new GroupUserClient();
        employeeClient = new EmployeeClient();
        shiftClient = new ShiftClient();
        shiftConfigClient = new ShiftConfigClient();
        selectedItem = new ShiftListData();

        searchData = new ShiftListData();
        searchData.setStartDate(new Date());
        searchData.setExpireDate(new Date());

        shiftPeopleDetailSelected = new ShiftConfigDto();

        lstPeople = employeeClient.listPeople(null, null, null);
        lstGroupCatg = groupUserClient.listGroupCatg(-1);
        lstGroupCatgChild = groupUserClient.listGroupCatg(null);
        lstShiftCatgAll = shiftClient.listShiftCatg();
        lstShiftCatg = new ArrayList<>(lstShiftCatgAll);

        changeMode(null, false);
        reloadListData();
    }

    private void reloadListData() {
        selectedItem = new ShiftListData();
        dataModel = new ShiftDataModel("tblData", searchData, shiftClient);
    }

    public void search() {
        reloadListData();
    }

    public void goToCreatePage(ShiftListData item) {
        if (item != null) {
            selectedItem = item;
        } else {
            updateMode = false;
            createMode = true;
            lstPeople = Collections.emptyList();
            lstGroupCatgChild = Collections.emptyList();
            lstShiftCatg = new ArrayList<>();
            selectedItem = new ShiftListData();
            shiftPeopleDetail = new ShiftPeopleDetail();
            shiftPeopleDetail.setShifts(new ArrayList<>());
            redirectPage("/shift-manager-detail.xhtml?faces-redirect=true");
        }
    }

    public void goToAddShift(ShiftConfigDto item) {
        if (item != null) {
            shiftPeopleDetailSelected = item;
        } else {
            shiftPeopleDetailSelected = new ShiftConfigDto();
            shiftPeopleDetailSelected.setStartDate(Utils.nowYMD());
            shiftPeopleDetailSelected.setExpireDate(Utils.nowYMD());
        }
    }

    public void addShift() {
        try {
            Date current = new Date();

            if (shiftPeopleDetailSelected.getCode() == null) {
                if (Utils.nowYMD().getTime() > Utils.truncDate(shiftPeopleDetailSelected.getStartDate()).getTime()) {
                    CommonFaces.showMessageError("Ngày đăng ký phải lớn hơn ngày hiện tại");
                    return;
                } else if (shiftPeopleDetailSelected.getExpireDate() != null && Utils.truncDate(shiftPeopleDetailSelected.getStartDate()).getTime() > Utils.truncDate(shiftPeopleDetailSelected.getExpireDate()).getTime()) {
                    CommonFaces.showMessageError("Ngày đăng ký phải nhỏ hơn ngày kết thúc");
                    return;
                }
            } else {
                if (shiftPeopleDetailSelected.getExpireDate() != null && Utils.truncDate(shiftPeopleDetailSelected.getStartDate()).getTime() > Utils.truncDate(shiftPeopleDetailSelected.getExpireDate()).getTime()) {
                    CommonFaces.showMessageError("Ngày đăng ký phải nhỏ hơn ngày kết thúc");
                    return;
                }
            }
            List<ShiftConfigDto> lstTimes = new ArrayList<>(mShiftCreate.values());
            boolean checkRange = ShiftDateValidate.checkRange(shiftPeopleDetailSelected, lstTimes);
            if (!checkRange) {
                CommonFaces.showMessageError("Khoảng thời gian đã tồn tại không hợp lệ");
                return;
            }

            if (shiftPeopleDetailSelected.getCode() == null) {
                String key = this.selectedItem.getPeopleId().concat("_").concat(shiftPeopleDetailSelected.getAutoid() + "");
                ShiftConfigDto shiftConfig = shiftConfigClient.shiftConfigDetail(shiftPeopleDetailSelected.getAutoid());
                shiftPeopleDetailSelected.setCode(shiftConfig.getCode());
                shiftPeopleDetailSelected.setName(shiftConfig.getName());
                shiftPeopleDetailSelected.setShiftDetail(shiftConfig.getShiftDetail());
                shiftPeopleDetailSelected.setShiftOrg(shiftConfig.getShiftOrg());
                shiftPeopleDetailSelected.setOshiftDetail(shiftConfig.getOshiftDetail());
                if (mShiftCreate.containsKey(mShiftCreate)) {
                    mShiftCreate.put(key, shiftPeopleDetailSelected);
                } else {
                    mShiftCreate.put(key, shiftPeopleDetailSelected);
                }
            }
            this.shiftPeopleDetail.setShifts(new ArrayList<>(mShiftCreate.values()));


            CommonFaces.hideDialog("dlgShiftVar");
//            CommonFaces.showGrowlInfo("Thêm mới thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public void editShift(ShiftConfigDto item) {
        try {
            this.shiftPeopleDetailSelected = item;
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public void deleteShift(ShiftConfigDto item) {
        try {
            this.shiftPeopleDetailSelected = item;
            String key = this.shiftPeopleDetail.getPeopleId().concat("_").concat(shiftPeopleDetailSelected.getAutoid() + "");
            mShiftCreate.remove(key);
            this.shiftPeopleDetail.setShifts(new ArrayList<>(mShiftCreate.values()));
//            CommonFaces.showGrowlInfo("Xóa ca thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }


    public void add() {
        try {
            Date current = new Date();
            selectedItem.setAutoid(null);
            selectedItem.setCreatedBy(SessionUtils.getUserName());
            selectedItem.setCreatedAt(current);
            if (selectedItem.getPeopleId() != null) {
                selectedItem.setShiftTarget("P");
            } else {
                selectedItem.setShiftTarget("G");
            }

            if (selectedItem.getDepId() != null && selectedItem.getGroupId() == null) {
                selectedItem.setGroupId(selectedItem.getDepId());
            }

            if (shiftPeopleDetail != null && CollectionUtils.isEmpty(shiftPeopleDetail.getShifts())) {
                CommonFaces.showMessageError("Ca làm việc không được trống");
                return;
            }

//            if (Utils.nowYMD().getTime() > selectedItem.getStartDate().getTime()) {
//                CommonFaces.showMessageError("Ngày đăng ký phải lớn hơn ngày hiện tại");
//                return;
//            } else if (selectedItem.getExpireDate() != null && selectedItem.getStartDate().getTime() > selectedItem.getExpireDate().getTime()) {
//                CommonFaces.showMessageError("Ngày đăng ký phải nhỏ hơn ngày kết thúc");
//                return;
//            }

            boolean save = false;
            if (mShiftCreate != null) {
                boolean del = shiftClient.delete(selectedItem.getPeopleId());
                if (!del) {
                    CommonFaces.showMessageError("Có lỗi sảy ra");
                    return;
                }
                ModelMapper mapper = new ModelMapper();
                mShiftCreate.values().forEach(item -> {
                    ShiftListData updateDto = mapper.map(selectedItem, ShiftListData.class);
                    updateDto.setStartDate(item.getStartDate());
                    updateDto.setExpireDate(item.getExpireDate());
                    updateDto.setShiftId(item.getAutoid());
                    updateDto.setShiftCode(item.getCode());
                    updateDto.setAutoid(null);
                    shiftClient.create(updateDto);
                });
                save = true;
                getDetail(selectedItem);
                shiftPeopleDetailSelected = new ShiftConfigDto();
            } else {
                CommonFaces.showMessageError("Ca làm việc không được trống");
                return;
            }
            if (save) {
                CommonFaces.hideDialog("dlgDetailVar");
                CommonFaces.showGrowlInfo("Thêm mới thành công!");
            } else {
                CommonFaces.showMessage("Thêm mới không thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
//        reloadListData();
    }

    public void update() {
        try {
            if (selectedItem.getPeopleId() != null) {
                selectedItem.setShiftTarget("P");
            } else {
                selectedItem.setShiftTarget("G");
            }

            if (selectedItem.getDepId() != null && selectedItem.getGroupId() == null) {
                selectedItem.setGroupId(selectedItem.getDepId());
            }

//            if (selectedItem.getExpireDate() != null && selectedItem.getStartDate().getTime() > selectedItem.getExpireDate().getTime()) {
//                CommonFaces.showMessageError("Ngày đăng ký phải nhỏ hơn ngày kết thúc");
//                return;
//            }

            boolean save = false;
            if (mShiftCreate != null) {
                boolean del = shiftClient.delete(selectedItem.getPeopleId());
                if (!del) {
                    CommonFaces.showMessageError("Có lỗi sảy ra");
                    return;
                }
                ModelMapper mapper = new ModelMapper();
                mShiftCreate.values().forEach(item -> {
                    ShiftListData updateDto = mapper.map(selectedItem, ShiftListData.class);
                    updateDto.setStartDate(item.getStartDate());
                    updateDto.setExpireDate(item.getExpireDate());
                    updateDto.setShiftId(item.getAutoid());
                    updateDto.setShiftCode(item.getCode());
                    updateDto.setAutoid(null);
                    shiftClient.create(updateDto);
                });
                save = true;
                getDetail(selectedItem);
                shiftPeopleDetailSelected = new ShiftConfigDto();
            } else {
                CommonFaces.showMessageError("Ca làm việc không được trống");
                return;
            }
            if (save) {
                if (!StringUtils.isEmpty(selectedItem.getErrorCode()) && !"S".equals(selectedItem.getErrorCode())) {
                    CommonFaces.showMessage("Sửa thông tin không thành công: (" + selectedItem.getErrorCode() + ")"
                            + selectedItem.getErrorMessage());
                } else {
                    CommonFaces.hideDialog("dlgDetailVar");
                    CommonFaces.showGrowlInfo("Sửa thông tin thành công!");
                }
            } else {
                CommonFaces.showMessage("Sửa thông tin không thành công! ");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
        this.updateMode = false;
        this.createMode = false;
//        reloadListData();
    }

    public void delete(ShiftListData item) {
        try {
            shiftClient.delete(item.getAutoid());
            CommonFaces.showMessage("Xóa thành công!");
            reloadListData();
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public void resetImportExport() {
        selectedItem = ShiftListData.builder().build();
        uploadExcelStatus = "";
        errorSummary = "";
        attachedErrorFileByteArr = null;
        content = null;
        disableErrorLink = true;
        importTitle = "";
        uploadedExcel = null;
    }

    public void closeDlgUploadStatus() {
        resetImportExport();
    }

    public void uploadExcelFile() {
        try {
            selectedItem = ShiftListData.builder().build();
            if (uploadedExcel == null || StringUtils.isEmpty(importTitle)) {
                return;
            }

            InputStream inputStream = uploadedExcel.getInputStream();
            byte[] byteArr = IOUtils.toByteArray(inputStream);

            Map<Integer, StringBuilder> mapExcelError = new HashMap();
            Set<String> errorSummarySet = new HashSet();
            ExcelTemplate excelTemplate = excelUtils
                    .getExcelTemplate(Constants.ExcelTemplateCodes.SHIFT_MANAGER_IMPORT);
            ExcelResponseMessage excelResponseMessage = new ExcelResponseMessage();
            String fileName = uploadedExcel.getFileName() != null ? uploadedExcel.getFileName() : "";

            if (!excelUtils.validFileType(fileName, excelResponseMessage, errorSummarySet)) {
                disableErrorLink = false;
                showResultDialog(null, errorSummarySet, null, false);
                return;
            }

            Workbook workbook = excelUtils.initWorkbook(byteArr, fileName);

            Sheet sheet = workbook.getSheetAt(excelTemplate.getSheetSeqNo());

            if (excelUtils.checkEmptyFile(sheet, excelTemplate.getStartRow(), excelResponseMessage, errorSummarySet)) {
                attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError,
                        excelTemplate);
                disableErrorLink = false;
                showResultDialog(null, errorSummarySet, null, false);
                return;
            }

            validateAndSaveExcelData(workbook, sheet, fileName, mapExcelError, errorSummarySet, excelTemplate);
            reloadListData();
            PrimeFaces current = PrimeFaces.current();
            current.ajax().update("frmImport");
//            current.executeScript("PF('dagUploadExcel').hide();");
//            current.executeScript("PF('dagUploadExcelStatus').show();");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void downloadFile() {
        try {
            String fileName = Constants.ExcelTemplateCodes.SHIFT_MANAGER_EXPORT + "_ERROR.xlsx";
            InputStream downloadFileInputStream = new ByteArrayInputStream(attachedErrorFileByteArr);
            DefaultStreamedContent content = DefaultStreamedContent.builder().name(fileName)
                    .stream(() -> downloadFileInputStream).build();
            this.content = content;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void downloadExampleFile() {
        try {
            DefaultStreamedContent content = DefaultStreamedContent.builder()
                    .name("import_calamviec.xlsx")
                    .contentType("application/vnd.ms-excel")
                    .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/import_calamviec.xlsx"))
                    .build();
            this.importFileExamplecontent = content;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void exportExcel() {
        ExcelTemplate excelTemplate = excelUtils.getExcelTemplate(Constants.ExcelTemplateCodes.SHIFT_MANAGER_EXPORT);
        ResponseListData<ShiftListData> responseListData = shiftClient.listAllShift(searchData.getDepId(), searchData.getPeopleId(),
                searchData.getFullName(), searchData.getGroupId(), 0, 100000);
        if (responseListData != null && responseListData.getData() != null) {
            responseListData.getData().forEach(item -> {
                GroupCatg groupCatg = groupUserClient.findGroupById(item.getGroupId());
                if (groupCatg != null) {
                    item.setGroupCode(groupCatg.getGroupCode());
                }

                ShiftCatg shiftCatg = shiftClient.findShiftCatgById(item.getShiftId());
                if (shiftCatg != null) {
                    item.setShiftCode(shiftCatg.getCode());
                }
            });
        }
        byte[] exportDataByteArr = excelUtils.exportExcel(responseListData.getData(), ShiftListData.class,
                excelTemplate);
        String fileName = Constants.ExcelTemplateCodes.SHIFT_MANAGER_EXPORT + ".xlsx";
        InputStream downloadFileInputStream = new ByteArrayInputStream(exportDataByteArr);
        exportExcelContent = DefaultStreamedContent.builder().name(fileName).stream(() -> downloadFileInputStream)
                .build();

    }


    public void showResultDialog(List<ShiftListData> shiftListError, Set<String> errorSummarySet, Integer total, boolean isPreImport) {
        errorSummary = null;
        if (shiftListError == null) {
            StringBuilder errorSummaryBuilder = new StringBuilder();
            errorSummarySet.forEach(cellError -> {
                errorSummaryBuilder.append(cellError);
            });
            errorSummary = errorSummaryBuilder.toString();
        } else {
            if (isPreImport) {
                disableErrorLink = false;
                uploadExcelStatus = MessageProvider.getValue("upload.excel.fail", null);
                errorSummary = "File dữ liệu không hợp lệ. Vui lòng tải file lỗi kiểm tra lại dữ liệu!";
            } else {
                if (shiftListError.size() == 0) {
                    uploadExcelStatus = MessageProvider.getValue("upload.excel.success", null);
                    disableErrorLink = true;
                } else {
                    disableErrorLink = false;
                    uploadExcelStatus = MessageProvider.getValue("upload.excel.fail", null);
                    if (total != null) {
                        errorSummary = String.format("Số lượng bản ghi thành công %d/%d, download file để xem chi tiết lỗi",
                                (total - shiftListError.size()), total);
                    } else {
                        errorSummary = String.format("Số lượng bản ghi lỗi %d, download file để xem chi tiết lỗi",
                                shiftListError.size());
                    }
                }
            }
        }
    }

    public void validateAndSaveExcelData(Workbook workbook, Sheet sheet, String fileName,
                                         Map<Integer, StringBuilder> mapExcelError, Set<String> errorSummarySet, ExcelTemplate excelTemplate) {
        List<ShiftListData> shiftListDataOrg = excelUtils.parseExcelToDto(ShiftListData.class, sheet, fileName,
                excelTemplate, mapExcelError, errorSummarySet);

        List<ShiftListData> shiftListData = new ArrayList<>(shiftListDataOrg);

        List<ShiftListData> employeeDataError = validateAndBuildListError(shiftListData, shiftListDataOrg, mapExcelError);

        Map<String, ShiftListData> mListErrorAfter = new HashMap<>();
        StringBuilder imagePath = new StringBuilder();
        StringBuilder imageUrl = new StringBuilder();
        handleUploadImage(imagePath, imageUrl);

        ImportData<ShiftListData> shiftImportDataRs = shiftClient.importShift(SessionUtils.getUserName(),
                importTitle, imageUrl.toString(), shiftListData);
        // if there is error while saving
        if (shiftImportDataRs != null && shiftImportDataRs.getObjectImport() != null) {
            shiftImportDataRs.getObjectImport().forEach(item -> {
                if (!StringUtils.isEmpty(item.getError()) && !"S".equals(item.getMessage())) {
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
            for (int i = 0; i < shiftListDataOrg.size(); i++) {
                ShiftListData item = shiftListDataOrg.get(i);
                String key = item.getPeopleId() + "_" + item.getGroupId();
                if (mListErrorAfter.containsKey(key)) {
                    ShiftListData itemError = mListErrorAfter.get(key);
                    Integer keyMapError = Integer.valueOf(i);
                    if (mapExcelError.containsKey(keyMapError)) {
                        mapExcelError.get(keyMapError).append("- " + itemError.getMessage());
                    } else {
                        mapExcelError.put(keyMapError, new StringBuilder("- " + itemError.getMessage()));
                    }
                    employeeDataError.add(itemError);
                }
            }
        }

        log.info("validateAndSaveExcelData: {}", shiftImportDataRs);
        if (!mapExcelError.isEmpty() || !errorSummarySet.isEmpty()
                || (employeeDataError != null && employeeDataError.size() > 0)) {
            attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError,
                    excelTemplate);
//                attachedErrorFileByteArr = excelUtils.exportExcel(employeeDataError, Employee.class,
//                        excelTemplate);
            showResultDialog(employeeDataError, errorSummarySet, shiftListDataOrg.size(), false);
        } else {
            disableErrorLink = true;
            showResultDialog(employeeDataError, errorSummarySet, shiftListDataOrg.size(), false);
            CommonFaces.hideDialog("dlgImportVar");
            CommonFaces.showGrowlInfo("Import thành công!");
            search();
        }
//        }
    }

    private List<ShiftListData> validateAndBuildListError(List<ShiftListData> shiftListData, List<ShiftListData> shiftListDataOrg,
                                                          Map<Integer, StringBuilder> mapExcelError) {
        List<ShiftListData> shiftListError = new ArrayList<>();
        List<GroupCatg> lstGroupIn = groupUserClient.listGroupCatg();
        for (int i = 0; i < shiftListData.size(); i++) {
            ShiftListData item = shiftListData.get(i);

            if (!StringUtils.isEmpty(item.getDepCode())) {
                GroupCatg depCatg = groupUserClient.findGroupByCode(lstGroupIn, item.getDepCode());
                if (depCatg != null) {
                    item.setDepId(depCatg.getGroupId());
                } else {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Mã khối không hợp lệ\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }


            GroupCatg groupCatg = null;
            if (!StringUtils.isEmpty(item.getGroupCode())) {
                groupCatg = groupUserClient.findGroupByCode(lstGroupIn, item.getGroupCode());
                if (groupCatg != null) {
                    item.setGroupId(groupCatg.getGroupId());
                } else {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Mã phòng không hợp lệ\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }

            ShiftCatg shiftCatg = shiftClient.findShiftCatgByCode(item.getShiftCode());
            if (shiftCatg != null) {
                item.setShiftId(shiftCatg.getAutoid());
            } else {
                Integer key = Integer.valueOf(i);
                String errorMgs = " - Mã ca không hợp lệ";
                if (!mapExcelError.containsKey(key)) {
                    mapExcelError.put(key, new StringBuilder(errorMgs));
                } else {
                    mapExcelError.get(key).append(errorMgs);
                }
            }

            if (item.getStartDate() == null) {
                Integer key = Integer.valueOf(i);
                String errorMgs = " - Ngày bắt đầu\n";
                if (!mapExcelError.containsKey(key)) {
                    mapExcelError.put(key, new StringBuilder(errorMgs));
                } else {
                    mapExcelError.get(key).append(errorMgs);
                }
            }

            if (item.getStartDate() != null && item.getExpireDate() != null) {
                if (item.getExpireDate() != null && item.getStartDate().getTime() > item.getExpireDate().getTime()) {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Ngày đăng ký phải nhỏ hơn ngày kết thúc\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }
        }

        if (mapExcelError != null && mapExcelError.size() > 0) {
            for (Integer key : mapExcelError.keySet()) {
                ShiftListData item = shiftListDataOrg.get(key.intValue());
                item.setError(mapExcelError.get(key).toString());
                item.setMessage(mapExcelError.get(key).toString());
                shiftListError.add(item);
            }
            shiftListError.forEach(item -> {
                shiftListData.remove(item);
            });
        }
        return shiftListError;
    }

    public void handleUploadImage(StringBuilder inputFilePath, StringBuilder imageUrl) {
        if (ObjectUtils.isEmpty(uploadedImage)) {
            return;
        }

        boolean checkRealImage = checkRealImg(uploadedImage);
        if (!checkRealImage) {
            CommonFaces.showMessageError(MessageProvider.getValue("upload.image.invalid.file"));
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
            try (OutputStream out = new FileOutputStream(targetFile);
                 InputStream in = uploadedImage.getInputStream();) {
                ExternalContext ext = FacesContext.getCurrentInstance().getExternalContext();
                URI uri = new URI(ext.getRequestScheme(), null, ext.getRequestServerName(), ext.getRequestServerPort(),
                        ext.getRequestContextPath(), null, null);
                // updateAd.setImage1(uri.toASCIIString() + "/images/" + fileName);
                // updateAd.setImage2(imageFolder + fileName);
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

    private boolean checkRealImg(UploadedFile f) {
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

    public void ftxtGroupValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            lstGroupCatgChild = groupUserClient.listGroupCatg((Integer.parseInt(val.toString())));
            searchData.setDepId((Integer.parseInt(val.toString())));
        }
    }

    public void ftxtGroupChildValueChange(SelectEvent<String> event) {
        String val = event.getObject();
        if (val != null && !val.isEmpty()) {
            searchData.setGid(Integer.parseInt(val));
        }
    }

    public void txtGroupValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            lstGroupCatgChild = groupUserClient.listGroupCatg((Integer.parseInt(val.toString())));
            Long depId = Long.parseLong(val.toString());
            List<Employee> lstEmpl = employeeClient.listPeople(depId, null, null);
            lstPeople = new ArrayList<>();
            if (!CollectionUtils.isEmpty(lstEmpl)) {
                lstEmpl.forEach(item -> {
                    if (Constants.RoleConstants.GROUP_LEADER.equals(item.getRole())) {
                        lstPeople.add(item);
                    }
                });
            }
            reInitShiftOrg(depId, null);
        }
    }

    public void txtGroupChildValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setGid(Integer.parseInt(val.toString()));
            Long groupId = Long.parseLong(val.toString());
            lstPeople = employeeClient.listPeople(null, groupId, null);
        }
    }

    public void txtPeopleValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setPeopleId(val.toString());
        }
    }

    private void reInitShiftOrg(Long orgId, Long groupId) {
        List<ShiftConfigOrgDto> lstShiftConfigDtos = shiftConfigClient.shiftConfigByGroup(orgId, groupId);
        List<ShiftCatg> lstShiftCatgNew = new ArrayList<>();
        if (lstShiftConfigDtos != null) {
            for (ShiftConfigOrgDto itemConfigOrg : lstShiftConfigDtos) {
                for (ShiftCatg itemCatg : lstShiftCatgAll) {
                    if (itemConfigOrg.getShiftCode().equals(itemCatg.getCode()) && !lstShiftCatgNew.contains(itemCatg)) {
                        lstShiftCatgNew.add(itemCatg);
                    }
                }
            }
        }
        lstShiftCatg.clear();
        lstShiftCatg.addAll(lstShiftCatgNew);
        selectedItem.setShiftConfig(null);
    }

    public void txtShiftValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        selectedItem.setShiftConfig(ShiftConfigDto.builder().build());
        if (val != null) {
            selectedItem.setShiftId(Integer.parseInt(val.toString()));
            ShiftConfigDto shiftConfig = shiftConfigClient.shiftConfigDetail(Integer.parseInt(val.toString()));
            selectedItem.setShiftConfig(shiftConfig);
        }
    }

    public void txtShiftValueChangeChildDialog(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            ShiftConfigDto shiftConfig = shiftConfigClient.shiftConfigDetail(Integer.parseInt(val.toString()));
            shiftPeopleDetailSelected.setShiftDetail(shiftConfig.getShiftDetail());
            shiftPeopleDetailSelected.setOshiftDetail(shiftConfig.getOshiftDetail());
            shiftPeopleDetailSelected.setOshiftDetail(shiftConfig.getOshiftDetail());
        }
    }

    public void onRowSelect(SelectEvent event) {
        selectedItem = (ShiftListData) event.getObject();
    }

    public void onSelect(ShiftListData item) {
        selectedItem = item;
    }

    public void changeMode(ShiftListData item, boolean updateMode) {
        if (item != null) {
            this.updateMode = updateMode;
            this.createMode = false;
            selectedItem = item;
            GroupCatg currentGroup = groupUserClient.findGroupById(selectedItem.getGroupId());
            List<GroupCatg> lstDep = groupUserClient.listGroupCatg(-1);
            if (item.getShiftCode() != null) {
                ShiftConfigDto shiftConfig = shiftConfigClient.shiftConfigDetail(item.getShiftCode());
                selectedItem.setShiftConfig(shiftConfig);
            }
            lstDep.forEach(g -> {
                if (currentGroup != null && g.getGroupId().equals(currentGroup.getParentId())) {
                    selectedItem.setDepId(g.getGroupId());
                    lstGroupCatgChild = groupUserClient.listGroupCatg(g.getGroupId());
                    return;
                }
            });
            getDetail(item);
            redirectPage("/shift-manager-detail.xhtml?faces-redirect=true");
        } else {
            this.updateMode = false;
            this.createMode = true;
            selectedItem = new ShiftListData();
        }
    }

    private void getDetail(ShiftListData item) {
        this.shiftPeopleDetail = shiftClient.getShiftPeopleDetail(item.getPeopleId());
        log.info("shiftPeopleDetail: {}", Utils.objToJson(shiftPeopleDetail));
        if (!CollectionUtils.isEmpty(this.shiftPeopleDetail.getShifts())) {
            this.shiftPeopleDetail.getShifts().forEach(s -> {
                String key = this.shiftPeopleDetail.getPeopleId().concat("_").concat(s.getAutoid() + "");
                mShiftCreate.put(key, s);
            });
        }
    }

    public boolean isViewDetail() {
        return false;
    }

    public boolean isEditable() {
        return createMode || updateMode;
    }

    public void goToUpdatePage() {
        updateMode = true;
        createMode = false;
    }

    public void redirectPage(String url) {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            ServletContext servletContext = (ServletContext) context.getCurrentInstance().getExternalContext()
                    .getContext();
            String path = servletContext.getContextPath() + "/faces";
            FacesContext.getCurrentInstance().getExternalContext().redirect(path + url);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            CommonFaces.showMessage(e.getMessage());
        }
    }
}
