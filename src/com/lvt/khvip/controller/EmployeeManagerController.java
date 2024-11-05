/**
 *
 */
package com.lvt.khvip.controller;

import com.lvt.khvip.client.EmployeeClient;
import com.lvt.khvip.client.GroupUserClient;
import com.lvt.khvip.client.RoleClient;
import com.lvt.khvip.client.author.dto.Roles;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.entity.ExcelResponseMessage;
import com.lvt.khvip.entity.ExcelTemplate;
import com.lvt.khvip.model.EmployeeDataModel;
import com.lvt.khvip.util.*;
import com.lvt.khvip.validate.CommonValidator;
import com.lvt.khvip.validate.EmailValidator;
import com.lvt.khvip.validate.EmployeeValidator;
import com.lvt.khvip.validate.UsernameValidator;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
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
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.*;

@ManagedBean
@SessionScoped
@Getter
@Setter
@Slf4j
public class EmployeeManagerController implements Serializable {
    private EmployeeClient employeeClient;
    private GroupUserClient groupUserClient;
    private RoleClient roleClient;
    private HttpUtils httpUtils;
    private EmployeeDataModel dataModel;
    private Employee searchData;
    private Employee selectedItem;
    private boolean updateMode;
    private boolean createMode;

    private List<GroupCatg> lstGroupCatg;
    private List<GroupCatg> lstGroupCatgChild;
    private List<Roles> lstRole;

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
    private String imageFolderPeople = ConfProperties.getProperty("image.folder.people");
    private String imageRootUrlPeople = ConfProperties.getProperty("image.root.url.people");

    private ChangePassworDto changePassworDto;

    private int tabIndex = 0;
    private String face;
    private List<List<String>> faces = new ArrayList<>();
    private UploadedFile fileUploadFace;
    private boolean flagIsLiveless = true;

    private Map<String, String> mapAvatarImport = new HashMap<>();

    @PostConstruct
    public void init() {
        groupUserClient = new GroupUserClient();
        employeeClient = new EmployeeClient();
        roleClient = new RoleClient();
        httpUtils = new HttpUtils();

        selectedItem = new Employee();
        searchData = new Employee();

        lstGroupCatg = groupUserClient.listGroupCatg(-1);
        lstGroupCatgChild = groupUserClient.listGroupCatg(null);
        lstRole = roleClient.listRole();

        changeMode(null, false);

        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String peopleId = params.get("peopleId");
        if (peopleId != null) {
            Employee emData = employeeClient.employeeDetail(peopleId);
            selectedItem = (emData != null) ? emData : null;
        }
        reloadListData();
    }

    private void reloadListData() {
        selectedItem = new Employee();
        dataModel = new EmployeeDataModel("tblData", searchData, employeeClient);
    }

    public void search() {
        reloadListData();
    }

    public void goToCreatePage(Employee item) {
        if (item != null) {
            selectedItem = item;
        } else {
            updateMode = false;
            createMode = true;
            selectedItem = new Employee();
            selectedItem.setRole("USER");
            this.tabIndex = 0;
            redirectPage("/employee-detail.xhtml?faces-redirect=true");
        }
    }

    public void goToUpdatePage() {
        updateMode = true;
        createMode = false;
    }

    public boolean validate() throws Exception {

        // tab đăng ký khuôn mặt
        if (tabIndex == 1) {
            if (faces == null || faces.size() <= 0) {
                CommonFaces.showMessage("Vui lòng chụp ảnh khuôn mặt. Được phép chụp tối đa 5 khuôn mặt");
                return false;
            }

            // check khuôn mặt
//            for (List<String> list : faces) {
//                for (String imageBase64 : list) {
//                    flagIsLiveless = httpUtils.checkLiveless(imageBase64);
//                    if (!flagIsLiveless) {
//                        break;
//                    }
//                }
//                if (!flagIsLiveless) {
//                    break;
//                }
//            }
//
//            if (!flagIsLiveless) {
////				showDialog("confirmReg");
//                return true;
//            } else {
//                return true;
//            }

            // tab đăng ký bằng ảnh tải lên
        } else if (tabIndex == 0) {
            if (selectedItem.getImagePath() == null) {
                CommonFaces.showMessageError("Vui lòng tải ảnh lên");
                return false;
            }
            return true;
        }
        return true;
    }

    public void add() {
        Connection connection = null;
        try {
//            connection = ConnectDB.getConnection();
//            connection.setAutoCommit(false);
            boolean valid = validate();
            if (valid) {
                if (tabIndex == 1) {
                    String filePath = rootPath + "/" + imageFolderPeople;
                    String imagePath = Utils.convertByte64ToFile(faces.get(0).get(0), filePath, selectedItem.getUserName());
                    selectedItem.setImagePath("/" + imageFolderPeople + "/" + selectedItem.getPeopleId() + "_" + System.currentTimeMillis() + ".jpg");
//                    boolean result = false;
//                    if (!flagIsLiveless) {
//                        result = PeopleDao.registerPeopleRegImage(selectedItem.getPeopleId(), faces, connection)
//                                && PeopleDao.registerPeopleWhitelist(selectedItem.getPeopleId(),
//                                User.getSessionUser().getId(), connection);
//                    } else {
//                        result = PeopleDao.registerPeopleRegImage(selectedItem.getPeopleId(), faces, connection);
//                    }
                }
                Date current = new Date();
                selectedItem.setId(null);
                if (Constants.RoleConstants.GROUP_LEADER.equals(selectedItem.getRole())) {
                    selectedItem.setGroupId(selectedItem.getDepId());
                }
                if (StringUtils.isEmpty(selectedItem.getCustomerType())) {
                    selectedItem.setCustomerType("3");
                }
                ResponseData<Employee> rs = employeeClient.create(selectedItem);
                if (!rs.isError()) {
//                    selectedItem = new Employee();
                    this.tabIndex = 0;
//                    redirectPage("/employee-manager.xhtml");
                    CommonFaces.showMessage("Thêm mới thành công!");
                } else {
                    if (rs.getMessage() != null) {
                        CommonFaces.showMessageError(rs.getMessage());
                    } else {
                        CommonFaces.showMessageError("Thêm mới không thành công!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
            return;
//            try {
//                connection.rollback();
//            } catch (SQLException e1) {
//                log.error(e.getMessage(), e);
//            }
        } finally {
//            Utility.closeObject(connection);
//			hideDialog("confirmReg");
        }
//        reloadListData();
    }

    public void update() {
        Connection connection = null;
        try {
//            connection = ConnectDB.getConnection();
//            connection.setAutoCommit(false);
            boolean valid = validate();
            if (valid) {
                if (tabIndex == 1) {
                    String filePath = rootPath + "/" + imageFolderPeople;
                    String imagePath = Utils.convertByte64ToFile(faces.get(0).get(0), filePath, selectedItem.getUserName());
                    selectedItem.setImagePath("/" + imageFolderPeople + "/" + selectedItem.getPeopleId() + ".jpg");
//                    boolean result = false;
//                    if (!flagIsLiveless) {
//                        result = PeopleDao.registerPeopleRegImage(selectedItem.getPeopleId(), faces, connection)
//                                && PeopleDao.registerPeopleWhitelist(selectedItem.getPeopleId(),
//                                User.getSessionUser().getId(), connection);
//                    } else {
//                        result = PeopleDao.registerPeopleRegImage(selectedItem.getPeopleId(), faces, connection);
//                    }
                }

                if (Constants.RoleConstants.GROUP_LEADER.equals(selectedItem.getRole())) {
                    selectedItem.setGroupId(selectedItem.getDepId());
                }
                if (StringUtils.isEmpty(selectedItem.getCustomerType())) {
                    selectedItem.setCustomerType("3");
                }
                if (StringUtils.isEmpty(selectedItem.getPassword()) && !StringUtils.isEmpty(selectedItem.getUserName())) {
                    selectedItem.setPassword(selectedItem.getUserName().concat("@123"));
                }
                ResponseData<Employee> rs = employeeClient.update(selectedItem);
                if (!rs.isError()) {
                    selectedItem = rs.getData();
//                    selectedItem = new Employee();
                    this.tabIndex = 0;
//                    redirectPage("/employee-manager.xhtml");
                    CommonFaces.showMessage("Sửa thông tin thành công!");
                } else {
                    if (rs.getMessage() != null) {
                        CommonFaces.showMessageError(rs.getMessage());
                    } else {
                        CommonFaces.showMessageError("Sửa thông tin không thành công! ");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
//            try {
//                connection.rollback();
//            } catch (SQLException e1) {
//                log.error(e.getMessage(), e);
//            }
        } finally {
//            Utility.closeObject(connection);
//			hideDialog("confirmReg");
        }
        this.updateMode = true;
        this.createMode = false;
    }

    public void delete(Employee item) {
        try {
            ResponseData<Boolean> rs = employeeClient.delete(item.getPeopleId());
            if (rs != null && !rs.isError()) {
                CommonFaces.showMessage("Xóa thành công!");
                reloadListData();
            } else {
                CommonFaces.showMessageError(rs.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public void resetPass() {
        try {
            employeeClient.resetPass(changePassworDto);
            CommonFaces.hideDialog("dlgResetPassVar");
            CommonFaces.showGrowlInfo("Cấp lại mật khẩu thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public void resetImportExport() {
        selectedItem = Employee.builder().build();
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

    public void handleUploadImage(StringBuilder inputFilePath, StringBuilder imageUrl) {
        if (ObjectUtils.isEmpty(uploadedImage)) {
            return;
        }

        String extension = uploadedImage.getFileName().substring(uploadedImage.getFileName().lastIndexOf(".") + 1);
        String fileName = "uploadedImage_" + System.currentTimeMillis() + "." + extension;
        String filePath = rootPath + "/" + imageFolderPeople + "/" + fileName;
        String filePathUnzip = rootPath + "/" + imageFolderPeople;
        File targetFile = new File(filePath);

        List<String> lstFile = new ArrayList<>();
        try (OutputStream out = new FileOutputStream(targetFile); InputStream in = uploadedImage.getInputStream();) {
            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            lstFile = ZipFileUnZipUtils.unzipzarFolder(Paths.get(filePath), Paths.get(filePathUnzip), "File ảnh phải nằm ngoài thư mục gốc");
            if(CollectionUtils.isEmpty(lstFile)){
                CommonFaces.showMessageError("Giải nén không thành công!");
                return;
            }
            log.info("unzip: {}", lstFile.size());
            if (lstFile.size() > 0) {
                lstFile.forEach(item -> {
                    String name = item.substring(0, item.lastIndexOf("."));
                    mapAvatarImport.put(name, item);
                });
            }
        } catch (Exception ex) {
            log.error("FAIL TO HANDLE FILE UPLOAD!", ex);
//            CommonFaces.showMessageError(ex.getMessage());
//            return;
        }

    }

    public void uploadExcelFile() {
        try {
            selectedItem = Employee.builder().build();
            if (uploadedExcel == null) {
                return;
            }

            InputStream inputStream = uploadedExcel.getInputStream();
            byte[] byteArr = IOUtils.toByteArray(inputStream);

            Map<Integer, StringBuilder> mapExcelError = new HashMap();
            Set<String> errorSummarySet = new HashSet();
            ExcelTemplate excelTemplate = excelUtils
                    .getExcelTemplate(Constants.ExcelTemplateCodes.EMPLOYEE_MANAGER_EXPORT);
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
            // current.ajax().update("frmImport");
            // current.ajax().update("tblData");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
//            CommonFaces.hideLoading();
        }
    }

    public void downloadFile() {
        try {
            String fileName = Constants.ExcelTemplateCodes.EMPLOYEE_MANAGER_EXPORT + "_ERROR.xlsx";
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
                    .name("import_employee.xlsx")
                    .contentType("application/vnd.ms-excel")
                    .stream(() -> FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/resources/import_employee.xlsx"))
                    .build();
            this.importFileExamplecontent = content;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void showResultDialog(List<Employee> shiftListError, Set<String> errorSummarySet, Integer total, boolean isPreImport) {
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
        List<Employee> employeeDataOrg = excelUtils.parseExcelToDto(Employee.class, sheet, fileName, excelTemplate,
                mapExcelError, errorSummarySet);

        List<Employee> employeeData = new ArrayList<>(employeeDataOrg);

        List<Employee> employeeDataError = validateAndBuildListError(employeeData, employeeDataOrg, mapExcelError);

//        if (employeeDataError.size() > 0) {
//            attachedErrorFileByteArr = excelUtils.createAttachedFile(workbook, sheet, fileName, mapExcelError,
//                    excelTemplate);
//            showResultDialog(employeeDataError, errorSummarySet, employeeData.size(), true);
//        } else {
        Map<String, Employee> mListErrorAfter = new HashMap<>();
        StringBuilder imagePath = new StringBuilder();
        StringBuilder imageUrl = new StringBuilder();
        handleUploadImage(imagePath, imageUrl);
        employeeData.forEach(item -> {
            if (mapAvatarImport.containsKey(item.getPeopleId())) {
                item.setImagePath("/" + imageFolderPeople + "/" + mapAvatarImport.get(item.getPeopleId()));
            }
        });
        ImportData<Employee> shiftImportDataRs = employeeClient.createBatch(SessionUtils.getUserName(), importTitle,
                imageUrl.toString(), employeeData);
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
            for (int i = 0; i < employeeDataOrg.size(); i++) {
                Employee item = employeeDataOrg.get(i);
                String key = item.getPeopleId() + "_" + item.getGroupId();
                if (mListErrorAfter.containsKey(key)) {
                    Employee itemError = mListErrorAfter.get(key);
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
            showResultDialog(employeeDataError, errorSummarySet, employeeDataOrg.size(), false);
        } else {
            disableErrorLink = true;
            showResultDialog(employeeDataError, errorSummarySet, employeeDataOrg.size(), false);
            CommonFaces.hideDialog("dlgImportVar");
            CommonFaces.showGrowlInfo("Import thành công!");
            search();
        }
//        }
    }

    public void exportExcel() {
        ExcelTemplate excelTemplate = excelUtils.getExcelTemplate(Constants.ExcelTemplateCodes.EMPLOYEE_MANAGER_EXPORT);
        ResponseListData<Employee> responseListData = employeeClient.listPeople(searchData.getPeopleId(), searchData.getFullName(), searchData.getMobilePhone(), StringUtils.isEmpty(searchData.getImagePath()) ? null : searchData.getImagePath(), 0, 100000);
        if (responseListData != null && responseListData.getData() != null) {
            responseListData.getData().forEach(item -> {
                item.setRole(item.getRoleDescription());
                item.setGroupCode(item.getGroupName());
                item.setDepCode(item.getDepName());
            });
        }
        byte[] exportDataByteArr = excelUtils.exportExcel(responseListData.getData(), Employee.class,
                excelTemplate);
        String fileName = Constants.ExcelTemplateCodes.EMPLOYEE_MANAGER_EXPORT + ".xlsx";
        InputStream downloadFileInputStream = new ByteArrayInputStream(exportDataByteArr);
        exportExcelContent = DefaultStreamedContent.builder().name(fileName).stream(() -> downloadFileInputStream)
                .build();

    }

    private List<Employee> validateAndBuildListError(List<Employee> employeeListData, List<Employee> employeeListDataOrg,
                                                     Map<Integer, StringBuilder> mapExcelError) {
        List<Employee> shiftListError = new ArrayList<>();
        List<GroupCatg> lstGroupIn = groupUserClient.listGroupCatg();
        for (int i = 0; i < employeeListData.size(); i++) {
            boolean valid = false;
            Employee item = employeeListData.get(i);


            if (!StringUtils.isEmpty(item.getPeopleId())) {
                EmployeeValidator employeeValidator = new EmployeeValidator();
                if (!employeeValidator.validate(item.getPeopleId())) {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Mã nhân viên không hợp lệ\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }

            if (!StringUtils.isEmpty(item.getUserName())) {
                UsernameValidator usernameValidator = new UsernameValidator();
                if (!usernameValidator.validate(item.getUserName())) {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - User không hợp lệ\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }

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

            if (Constants.RoleConstants.GROUP_LEADER.equals(item.getRole())) {
                item.setGroupId(item.getDepId());
            } else {
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
                } else {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Phải nhập giá trị cho trường Phòng ban\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }

            if (!StringUtils.isEmpty(item.getGender())) {
                if (!"Nam".equals(item.getGender()) && !"Nữ".equals(item.getGender())) {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Giới tính không hợp lệ (Nam/Nữ)\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }

            if (!StringUtils.isEmpty(item.getMobilePhone())) {
                if (!CommonValidator.isValidMobiNumber(item.getMobilePhone())) {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Số điện thoại không hợp lệ\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }

            if (item.getDateOfBirth() == null) {
                Integer key = Integer.valueOf(i);
                String errorMgs = " - Ngày sinh không hợp lệ\n";
                if (!mapExcelError.containsKey(key)) {
                    mapExcelError.put(key, new StringBuilder(errorMgs));
                } else {
                    mapExcelError.get(key).append(errorMgs);
                }
            }

            if (!StringUtils.isEmpty(item.getEmail())) {
                EmailValidator emailValidator = new EmailValidator();
                if (!emailValidator.matcher(item.getEmail())) {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Email không hợp lệ\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }

            if (!StringUtils.isEmpty(item.getRole())) {
                for (Roles role : lstRole) {
                    if (role.getName().equals(item.getRole())) {
                        valid = true;
                        break;
                    }
                }
                if (!valid) {
                    Integer key = Integer.valueOf(i);
                    String errorMgs = " - Nhóm quyền không hợp lệ\n";
                    if (!mapExcelError.containsKey(key)) {
                        mapExcelError.put(key, new StringBuilder(errorMgs));
                    } else {
                        mapExcelError.get(key).append(errorMgs);
                    }
                }
            }
            if (StringUtils.isEmpty(item.getPassword()) && !StringUtils.isEmpty(item.getUserName())) {
                item.setPassword(item.getUserName().concat("@123"));
            }
            item.setCustomerType("3");
        }
        if (mapExcelError != null && mapExcelError.size() > 0) {
            for (Integer key : mapExcelError.keySet()) {
                Employee item = employeeListDataOrg.get(key.intValue());
                item.setError(mapExcelError.get(key).toString());
                item.setMessage(mapExcelError.get(key).toString());
                shiftListError.add(item);
            }
            shiftListError.forEach(item -> {
                employeeListData.remove(item);
            });
        }
        return shiftListError;
    }



    public void fileUploadFaceHandle(FileUploadEvent event) {
        String extension = event.getFile().getFileName().substring(event.getFile().getFileName().lastIndexOf(".") + 1);
        String fileName = selectedItem.getPeopleId() + "_" + System.currentTimeMillis() + "." + extension;
        String filePath = rootPath + "/" + imageFolderPeople + "/" + fileName;
        File targetFile = new File(filePath);
        long imageSize = event.getFile().getSize();
        if (imageSize > 1000000) {
            BufferedImage resizedImage = resizeImage(event.getFile());
            try {
                ImageIO.write(resizedImage, extension, targetFile);
            } catch (Exception ex) {
                log.error("FAIL TO HANDLE FILE UPLOAD!", ex);
            }
        } else {
            try (OutputStream out = new FileOutputStream(targetFile);
                 InputStream in = event.getFile().getInputStream();) {
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
        log.info("filePath: {}", filePath);
        log.info("image: {}", (imageFolderPeople + "/" + fileName));
        selectedItem.setImagePath("/" + imageFolderPeople + "/" + fileName);
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
        String val = event.getObject();
        if (val != null && !val.isEmpty()) {
            searchData.setGroupId(Integer.parseInt(val));
        }
    }

    public void txtGroupValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            lstGroupCatgChild = groupUserClient.listGroupCatg((Integer.parseInt(val.toString())));
        }
    }

    public void txtGroupChildValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setGroupId(Integer.parseInt(val.toString()));
        }
    }

    public void txtGenderValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setGender(val.toString());
        }
    }

    public void txtRoleValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setRole(val.toString());
            if(Constants.RoleConstants.GROUP_LEADER.equals(selectedItem.getRole())) {
                selectedItem.setGroupId(null);
                selectedItem.setDepId(null);
            }
        }
    }

    public void onRowSelect(SelectEvent event) {
        selectedItem = (Employee) event.getObject();
    }

    public void onSelect(Employee item) {
        selectedItem = item;
    }

    public void onTabChange(TabChangeEvent event) {
        tabIndex = ((TabView) event.getSource()).getIndex();
    }

    public void addFace() {
        if (!StringUtils.isEmpty(face)) {
            log.info("addFace {}", faces.size());
            if (faces.size() < 5) {
                faces.add(Arrays.asList(org.apache.commons.lang3.StringUtils.split(face, ";")));
            }
        }
    }

    public String geetEmployeeStatusName(String status) {
        if ("2".equals(status)) {
            return "Khóa";
        } else if ("1".equals(status)) {
            return "Hoạt động";
        } else if ("3".equals(status)) {
            return "Đóng";
        }
        return "N/A";
    }

    public void removeFace(String index) {
        faces.remove(Integer.valueOf(index).intValue());
    }

    public void showResetPass(Employee item) {
        selectedItem = item;
        changePassworDto = ChangePassworDto.builder().userName(selectedItem.getUserName())
                .newPassword(selectedItem.getUserName().concat("@123")).build();
    }

    public void changeMode(Employee item, boolean updateMode) {
        if (item != null) {
            this.updateMode = updateMode;
            this.createMode = false;
            selectedItem = item;
            GroupCatg currentGroup = groupUserClient.findGroupById(selectedItem.getGroupId());
            List<GroupCatg> lstDep = groupUserClient.listGroupCatg(-1);
            lstDep.forEach(g -> {
                if (currentGroup != null && g.getGroupId().equals(currentGroup.getParentId())) {
                    selectedItem.setDepId(g.getGroupId());
                    lstGroupCatgChild = groupUserClient.listGroupCatg(g.getGroupId());
                    return;
                }
            });
            redirectPage("/employee-detail.xhtml?faces-redirect=true");
        } else {
            this.updateMode = false;
            this.createMode = true;
            selectedItem = new Employee();
        }
    }

    public boolean isViewDetail() {
        return true;
    }

    public boolean isEditable() {
        return createMode || updateMode;
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
