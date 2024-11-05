/**
 *
 */
package com.lvt.khvip.controller;

import com.lvt.khvip.client.EmployeeClient;
import com.lvt.khvip.client.GroupUserClient;
import com.lvt.khvip.client.RoleClient;
import com.lvt.khvip.client.ShiftClient;
import com.lvt.khvip.client.author.dto.Roles;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.dao.ConnectDB;
import com.lvt.khvip.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.file.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@ManagedBean
@ViewScoped
@Getter
@Setter
@Slf4j
public class ProfileController implements Serializable {
    private ShiftClient shiftClient;
    private EmployeeClient employeeClient;
    private GroupUserClient groupUserClient;
    private RoleClient roleClient;

    private HttpUtils httpUtils;
    private Employee selectedItem;
    private boolean updateMode;
    private boolean createMode;
    private ChangePassworDto changePassworDto;

    private List<GroupCatg> lstGroupCatg;
    private List<GroupCatg> lstGroupCatgChild;
    private List<Roles> lstRole;

    private ProfileShift profileShift;

    private int tabIndex = 0;
    private String face;
    private List<List<String>> faces = new ArrayList<>();
    private UploadedFile fileUploadFace;
    private boolean flagIsLiveless = true;

    private String rootPath = ConfProperties.getProperty("root.path");
    private String imageFolderPeople = ConfProperties.getProperty("image.folder.people");
    private String imageRootUrlPeople = ConfProperties.getProperty("image.root.url.people");

    @PostConstruct
    public void init() {
        groupUserClient = new GroupUserClient();
        employeeClient = new EmployeeClient();
        roleClient = new RoleClient();
        shiftClient = new ShiftClient();
        httpUtils = new HttpUtils();

        selectedItem = new Employee();
        changePassworDto = new ChangePassworDto();

        lstGroupCatg = groupUserClient.listGroupCatg(-1);
        lstGroupCatgChild = groupUserClient.listGroupCatg(null);
        lstRole = roleClient.listRole();

        reInit();
    }

    public void reInit() {
        Employee employee = employeeClient.employeeDetail(SessionUtils.getPeopleId());
        if (employee != null) {
            selectedItem = employee;
            changePassworDto.setUserName(selectedItem.getUserName());
        }

        ProfileShift profileShiftRs = shiftClient.getProfileShift(selectedItem.getPeopleId());
        if (profileShiftRs != null && profileShiftRs.getShiftDetail() != null) {
            profileShift = profileShiftRs;
        }
    }

    public void update() {
        Connection connection = null;
        try {
            connection = ConnectDB.getConnection();
            connection.setAutoCommit(false);
            boolean valid = true;// validate();
            if (valid) {
                ResponseData<Employee> rs = employeeClient.update(selectedItem);
                if (!rs.isError()) {
                    selectedItem = rs.getData();
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
            try {
                connection.rollback();
            } catch (SQLException e1) {
                log.error(e.getMessage(), e);
            }
        } finally {
            Utility.closeObject(connection);
//			hideDialog("confirmReg");
        }
        this.updateMode = false;
        this.createMode = false;
    }

    public void preChangePass() {
        if (changePassworDto != null) {
            changePassworDto.setUserName(SessionUtils.getUserName());
            changePassworDto.setNewPassword(null);
            changePassworDto.setNewPasswordCf(null);
            changePassworDto.setPassword(null);
        }
    }

    public void changePass() {
        try {

            if (!changePassworDto.getNewPassword().equals(changePassworDto.getNewPasswordCf())) {
                CommonFaces.showMessageError("Xác nhận mật khẩu không khớp!");
                return;
            }

            ResponseData<Boolean> rs = employeeClient.changePass(changePassworDto);
            if (rs != null && !rs.isError()) {
                CommonFaces.hideDialog("dlgChangePassVar");
                CommonFaces.showDialog("dlgReloginVar");
//                CommonFaces.showGrowlInfo("Đổi mật khẩu thành công!");
            } else {
                CommonFaces.showMessageError("Mật khẩu cũ không đúng");
            }

        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        } finally {

        }
        this.updateMode = false;
        this.createMode = false;
    }

    public void reLogin() {
        HttpSession session = SessionUtils.getSession();
        session.invalidate();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        redirectPage("/login.xhtml");
    }

    public void fileUploadFaceHandle(FileUploadEvent event) {
        String extension = event.getFile().getFileName().substring(event.getFile().getFileName().lastIndexOf(".") + 1);
        String fileName = selectedItem.getUserName() + "_" + System.currentTimeMillis() + "." + extension;
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
        employeeClient.update(selectedItem);
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

    public void onTabChange(TabChangeEvent event) {
        tabIndex = ((TabView) event.getSource()).getIndex();
    }

    public void goToCreatePage(Employee item) {
        if (item != null) {
            selectedItem = item;
        } else {
            updateMode = false;
            createMode = true;
            selectedItem = new Employee();
            selectedItem.setRole("USER");
            redirectPage("/employee-detail.xhtml?faces-redirect=true");
        }
    }

    public void goToUpdatePage() {
        updateMode = true;
        createMode = false;
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

}
