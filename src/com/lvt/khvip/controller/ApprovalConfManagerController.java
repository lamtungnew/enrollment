/**
 *
 */
package com.lvt.khvip.controller;

import com.lvt.khvip.client.*;
import com.lvt.khvip.client.author.dto.Roles;
import com.lvt.khvip.client.dto.ApParamDto;
import com.lvt.khvip.client.dto.ApprovalConfDto;
import com.lvt.khvip.client.dto.GroupCatg;
import com.lvt.khvip.client.dto.ResponseData;
import com.lvt.khvip.util.CommonFaces;
import com.lvt.khvip.util.SessionUtils;
import com.lvt.khvip.util.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@ManagedBean
@ViewScoped
@Getter
@Setter
@Slf4j
public class ApprovalConfManagerController implements Serializable {
    private RoleClient roleClient;
    private ShiftClient shiftClient;
    private GroupUserClient groupUserClient;
    private ApParamClient apParamClient;
    private ApprovalConfClient approvalConfClient;
    private List<ApprovalConfDto> dataModel;
    private ApprovalConfDto searchData;
    private ApprovalConfDto selectedItem;
    private boolean updateMode;

    private List<GroupCatg> lstGroupCatg;
    private List<GroupCatg> lstGroupCatgChild;
    private List<ApParamDto> lstApprovalType;
    private List<Roles> lstApprover;

    @PostConstruct
    public void init() {
        roleClient = new RoleClient();
        approvalConfClient = new ApprovalConfClient();
        groupUserClient = new GroupUserClient();
        shiftClient = new ShiftClient();
        apParamClient = new ApParamClient();
        selectedItem = new ApprovalConfDto();
        searchData = new ApprovalConfDto();

        lstGroupCatg = groupUserClient.listGroupCatg(-1);
        lstGroupCatgChild = groupUserClient.listGroupCatg();
        lstApprovalType = apParamClient.listApParam("APPROVAL_TYPE");
        lstApprover = roleClient.listRole(true);

        changeMode(null);
        reloadListData();
    }

    private void reloadListData() {
        dataModel = approvalConfClient.listApproval();
    }

    public void search() {
        reloadListData();
    }

    public void goToCreatePage(ApprovalConfDto item) {
        if (item != null) {
            selectedItem = item;
        } else {
            updateMode = false;
            selectedItem = new ApprovalConfDto();
        }
    }

    public void add() {
        try {
            Date current = new Date();
            selectedItem.setAutoid(null);
            selectedItem.setCreatedAt(current);
            selectedItem.setModifiedAt(current);
            selectedItem.setCreatedBy(SessionUtils.getUserName());
            if (selectedItem.getApprovalLevel().equals(1)) {
                selectedItem.setApprovalLevel2(null);
            } else if (selectedItem.getApprovalLevel().equals(2)) {
                if (selectedItem.getApprovalLevel1().equals(selectedItem.getApprovalLevel2())) {
                    CommonFaces.showMessageError("Thứ tự duyệt không được trùng nhau !");
                    return;
                }
            }
            ResponseData<ApprovalConfDto> responseData = approvalConfClient.create(selectedItem);
            if (responseData != null && StringUtils.isEmpty(responseData.getError())) {
                selectedItem = responseData.getData();
                CommonFaces.hideDialog("dlgDetailVar");
                CommonFaces.showGrowlInfo("Thêm mới thành công!");
            } else {
                CommonFaces.showMessageError("Thêm mới không thành công", responseData.getMessage());
            }
            reloadListData();
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public void update() {
        try {
            if(!approvalConfClient.isEditableApprovalExistedER(selectedItem.getAutoid())){
                CommonFaces.showMessageError("Đang tồn tại đề xuất chưa được duyệt với quy trình trước đó");
                return;
            }
            selectedItem.setModifiedAt(new Date());
            if (selectedItem.getApprovalLevel().equals(1)) {
                selectedItem.setApprovalLevel2(null);
            } else if (selectedItem.getApprovalLevel().equals(2)) {
                if (selectedItem.getApprovalLevel1().equals(selectedItem.getApprovalLevel2())) {
                    CommonFaces.showMessageError("Thứ tự duyệt không được trùng nhau !");
                    return;
                }
            }
            ResponseData<ApprovalConfDto> responseData = approvalConfClient.update(selectedItem);
            if (responseData != null && StringUtils.isEmpty(responseData.getError())) {
                selectedItem = responseData.getData();
                CommonFaces.hideDialog("dlgDetailVar");
                CommonFaces.showGrowlInfo("Sửa thông tin thành công!");
            } else {
                CommonFaces.showMessageError("Sửa thông tin không thành công", responseData.getMessage());
            }
            reloadListData();
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public void delete(ApprovalConfDto item) {
        try {
            if(!approvalConfClient.isEditableApprovalExistedER(item.getAutoid())){
                CommonFaces.showMessageError("Đang tồn tại đề xuất chưa được duyệt với quy trình trước đó");
                return;
            }
            boolean rs = approvalConfClient.delete(item.getAutoid());
            if (rs) {
                CommonFaces.showMessage("Xóa thành công!");
                reloadListData();
                selectedItem = null;
            } else {
                CommonFaces.showMessageError("Xóa không thành công!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            CommonFaces.showMessageError(e.getMessage());
        }
    }

    public String getGroupNameById(Integer id) {
        GroupCatg group = groupUserClient.findGroupById(id);
        if (group != null)
            return group.getGroupName();
        return null;
    }

    public String getApproverDescription(String value) {
        for (Roles r : lstApprover) {
            if (r.getName().equals(value))
                return r.getDescription();
        }
        return null;
    }

    public String getApproveTypeDescription(String value) {
        for (ApParamDto ap : lstApprovalType) {
            if (ap.getValue().equals(value))
                return ap.description;
        }
        return null;
    }

    public void onRowSelect(SelectEvent event) {
        selectedItem = (ApprovalConfDto) event.getObject();
    }

    public void onSelect(ApprovalConfDto item) {
        selectedItem = item;
    }

    public void txtGroupValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            GroupCatg groupCatg = groupUserClient.findGroupById(Integer.valueOf(val.toString()));
            if (groupCatg != null) {
                lstGroupCatgChild = groupUserClient.listGroupCatg(groupCatg.getGroupId());
            }
        }
    }

    public void txtGroupChildValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setGroupId(Integer.valueOf(val.toString()));
        }
    }

    public void txtApprovalTypeValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setApprovalType(val.toString());
        }
    }

    public void txtApprovalLevelValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            if ("1".equals(val.toString())) {
                selectedItem.setApprovalLevel2(null);
            }
        }
    }

    public void txtLevel1ValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setApprovalLevel1(val.toString());
        }
    }

    public void txtLevel2ValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            selectedItem.setApprovalLevel2(val.toString());
        }
    }

    public void changeMode(ApprovalConfDto item) {
        if (item != null) {
            updateMode = true;
            selectedItem = item;
            GroupCatg groupCatg = groupUserClient.findGroupById(selectedItem.getGroupId());
            if (groupCatg != null) {
                lstGroupCatgChild = Arrays.asList(groupCatg);
            }
        } else {
            updateMode = false;
            selectedItem = new ApprovalConfDto();
        }
    }

    public boolean isViewDetail() {
        return false;
    }

}
