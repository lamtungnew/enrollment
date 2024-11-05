/**
 *
 */
package com.lvt.khvip.controller;

import com.lvt.khvip.client.ApParamClient;
import com.lvt.khvip.client.GroupUserClient;
import com.lvt.khvip.client.ShiftConfigClient;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.util.CommonFaces;
import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.primefaces.event.SelectEvent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@ManagedBean
@ViewScoped
@Getter
@Setter
@Slf4j
public class ShiftConfManagerController implements Serializable {
    private ShiftConfigClient shiftConfigClient;
    private GroupUserClient groupUserClient;
    private ApParamClient apParamClient;

    private List<ShiftConfigDto> dataModel;
    private ShiftConfigDto searchData;
    private ShiftConfigDto selectedItem;
    private boolean updateMode;

    private List<GroupCatg> lstGroupCatg;
    private List<GroupCatg> lstGroupCatgChild;
    private List<GroupCatg> lstAllGroupCatg;

    private ShiftConfigDetailDto shiftConfigDetailDto;
    private ShiftConfigDetailDto oshiftConfigDetailDto;
    private ShiftConfigOrgDto shiftConfigOrgDto;

    @PostConstruct
    public void init() {
        groupUserClient = new GroupUserClient();
        shiftConfigClient = new ShiftConfigClient();
        apParamClient = new ApParamClient();
        selectedItem = new ShiftConfigDto();
        searchData = new ShiftConfigDto();

        lstAllGroupCatg = groupUserClient.listGroupCatg();
        lstGroupCatg = groupUserClient.listGroupCatg(lstAllGroupCatg, -1);
        lstGroupCatgChild = new ArrayList<>();

        changeMode(null);
        reloadListData();
    }

    private void reloadListData() {
        dataModel = shiftConfigClient.listShiftConfig();
    }

    public void search() {
        reloadListData();
    }

    public void goToCreatePage(ShiftConfigDto item) {
        if (item != null) {
            selectedItem = getDetailSelected(item.getAutoid());
            selectedItem.setAutoid(null);
            buildDetail();
        } else {
            updateMode = false;
            selectedItem = new ShiftConfigDto();
            buildDetail();
        }
    }

    private ShiftConfigDto getDetailSelected(Integer id) {
        return shiftConfigClient.shiftConfigDetail(id);
    }


    private ShiftConfigDetailDto getDayOfWeekInList(int dayOfWeek, List<ShiftConfigDetailDto> shiftDetail) {
        if (CollectionUtils.isEmpty(shiftDetail)) {
            return null;
        } else {
            for (ShiftConfigDetailDto item : shiftDetail) {
                if (item != null && dayOfWeek == item.getDayInt()) {
                    return item;
                }
            }
        }
        return null;
    }

    private String buildWorkingDayType(List<ShiftConfigDetailDto> shiftDetail) {
        if (CollectionUtils.isEmpty(shiftDetail)) {
            return null;
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            for (ShiftConfigDetailDto item : shiftDetail) {
                if (item != null && !StringUtils.isEmpty(item.getDay())) {
                    stringBuilder.append("T" + item.getDay());
                }
            }
            return stringBuilder.toString();
        }
    }


    public void add() {
        try {
            Date current = new Date();
            selectedItem.setAutoid(null);
            selectedItem.setStatus("1");
            selectedItem.setWorkingDayType(buildWorkingDayType(selectedItem.getShiftDetail()));

            ShiftConfigDto shiftClean = cleanDetailTimeEmpty(selectedItem);
            ResponseListData<ShiftConfigDetailDto> validate = validateTimeDetail(shiftClean, false);

            if (validate != null) {
                CommonFaces.showMessageError(validate.getMessage());
                setDetailTimeError(selectedItem, validate.getData());
                return;
            }

            ShiftConfigDto responseData = shiftConfigClient.create(shiftClean);
            if (responseData != null && StringUtils.isEmpty(responseData.getError())) {
                selectedItem = getDetailSelected(responseData.getAutoid());
                buildDetail();
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
            selectedItem.setWorkingDayType(buildWorkingDayType(selectedItem.getShiftDetail()));

            ShiftConfigDto shiftClean = cleanDetailTimeEmpty(selectedItem);
            ResponseListData<ShiftConfigDetailDto> validate = validateTimeDetail(shiftClean, true);

            if (validate != null) {
                CommonFaces.showMessageError(validate.getMessage());
                setDetailTimeError(selectedItem, validate.getData());
                return;
            }

            ShiftConfigDto responseData = shiftConfigClient.update(cleanDetailTimeEmpty(shiftClean));
            if (responseData != null && StringUtils.isEmpty(responseData.getError())) {
                selectedItem = getDetailSelected(responseData.getAutoid());
                buildDetail();
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

    public void delete(ShiftConfigDto item) {
        try {
            boolean rs = shiftConfigClient.delete(item.getAutoid());
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

    public void copyTimeItem(ShiftConfigDetailDto shiftConfigDetailDto) {
        for (ShiftConfigDetailDto item : selectedItem.getShiftDetail()) {
            if (item.getDayInt().equals(shiftConfigDetailDto.getDayInt() + 1)) {
                item.setWorkingStartTime(shiftConfigDetailDto.getWorkingStartTime());
                item.setWorkingEndTime(shiftConfigDetailDto.getWorkingEndTime());
                item.setBreakStartTime(shiftConfigDetailDto.getBreakStartTime());
                item.setBreakEndTime(shiftConfigDetailDto.getBreakEndTime());
                item.setShiftMiddleStart(shiftConfigDetailDto.getShiftMiddleStart());
                item.setShiftMiddleEnd(shiftConfigDetailDto.getShiftMiddleEnd());

                item.setWorkingStartTimeLT(shiftConfigDetailDto.getWorkingStartTimeLT());
                item.setWorkingEndTimeLT(shiftConfigDetailDto.getWorkingEndTimeLT());
                item.setBreakStartTimeLT(shiftConfigDetailDto.getBreakStartTimeLT());
                item.setBreakEndTimeLT(shiftConfigDetailDto.getBreakEndTimeLT());
                item.setShiftMiddleStartLT(shiftConfigDetailDto.getShiftMiddleStartLT());
                item.setShiftMiddleEndLT(shiftConfigDetailDto.getShiftMiddleEndLT());
            }
        }
    }

    public void copyTimeOItem(ShiftConfigDetailDto shiftConfigDetailDto) {
        for (ShiftConfigDetailDto item : selectedItem.getOshiftDetail()) {
            if (item.getDayInt().equals(shiftConfigDetailDto.getDayInt() + 1)) {
                item.setWorkingStartTime(shiftConfigDetailDto.getWorkingStartTime());
                item.setWorkingEndTime(shiftConfigDetailDto.getWorkingEndTime());
                item.setBreakStartTime(shiftConfigDetailDto.getBreakStartTime());
                item.setBreakEndTime(shiftConfigDetailDto.getBreakEndTime());
                item.setShiftMiddleStart(shiftConfigDetailDto.getShiftMiddleStart());
                item.setShiftMiddleEnd(shiftConfigDetailDto.getShiftMiddleEnd());

                item.setWorkingStartTimeLT(shiftConfigDetailDto.getWorkingStartTimeLT());
                item.setWorkingEndTimeLT(shiftConfigDetailDto.getWorkingEndTimeLT());
                item.setBreakStartTimeLT(shiftConfigDetailDto.getBreakStartTimeLT());
                item.setBreakEndTimeLT(shiftConfigDetailDto.getBreakEndTimeLT());
                item.setShiftMiddleStartLT(shiftConfigDetailDto.getShiftMiddleStartLT());
                item.setShiftMiddleEndLT(shiftConfigDetailDto.getShiftMiddleEndLT());
            }
        }
    }

    public void clearTimeItem(ShiftConfigDetailDto item) {
                item.setWorkingStartTime(null);
                item.setWorkingEndTime(null);
                item.setBreakStartTime(null);
                item.setBreakEndTime(null);
                item.setShiftMiddleStart(null);
                item.setShiftMiddleEnd(null);

                item.setWorkingStartTimeLT(null);
                item.setWorkingEndTimeLT(null);
                item.setBreakStartTimeLT(null);
                item.setBreakEndTimeLT(null);
                item.setShiftMiddleStartLT(null);
                item.setShiftMiddleEndLT(null);
    }

    public void clearTimeOItem(ShiftConfigDetailDto item) {
        item.setWorkingStartTime(null);
        item.setWorkingEndTime(null);
        item.setBreakStartTime(null);
        item.setBreakEndTime(null);
        item.setShiftMiddleStart(null);
        item.setShiftMiddleEnd(null);

        item.setWorkingStartTimeLT(null);
        item.setWorkingEndTimeLT(null);
        item.setBreakStartTimeLT(null);
        item.setBreakEndTimeLT(null);
        item.setShiftMiddleStartLT(null);
        item.setShiftMiddleEndLT(null);
    }
/*    public void timeSelectListener(final TimeSelectEvent<Date> timeSelectEvent) {

    }*/

    public void timeSelectListener(ShiftConfigDetailDto shiftConfigDetailDto) {
        shiftConfigDetailDto = timeConvertLTToStr(shiftConfigDetailDto);
        log.info("shiftConfigDetailDto: {}", shiftConfigDetailDto);
    }

    public void overTimeValueChange(ShiftConfigDetailDto shiftConfigDetailDto) {
        selectedItem.getOshiftDetail().forEach(item -> {
            if (shiftConfigDetailDto.getIsOverTime() != null && shiftConfigDetailDto.getDay().equals(item.getDay())) {
                item.setWorkingStartTime(null);
                item.setWorkingEndTime(null);
                item.setBreakStartTime(null);
                item.setBreakEndTime(null);
                item.setShiftMiddleStart(null);
                item.setShiftMiddleEnd(null);
                item.setIsOverTime(shiftConfigDetailDto.getIsOverTime());
            }
        });
    }

    public String getGroupNameById(Integer id) {
        GroupCatg group = groupUserClient.findGroupById(lstAllGroupCatg, id);
        if (group != null)
            return group.getGroupName();
        return null;
    }

    public void onRowSelect(SelectEvent event) {
        selectedItem = (ShiftConfigDto) event.getObject();
    }

    public void onSelect(ShiftConfigDto item) {
        selectedItem = item;
    }

    public void txtGroupValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            GroupCatg groupCatg = groupUserClient.findGroupById(lstAllGroupCatg, Integer.valueOf(val.toString()));
            if (groupCatg != null) {
                lstGroupCatgChild = groupUserClient.listGroupCatg(lstAllGroupCatg, groupCatg.getGroupId());
            }
        }
    }

    public void txtGroupChildValueChange(SelectEvent<String> event) {
        Object val = event.getObject();
        if (val != null) {
            GroupCatg groupCatg = groupUserClient.findGroupById(lstAllGroupCatg, Integer.parseInt(val.toString()));
            GroupCatg depCatg = groupUserClient.findGroupById(lstAllGroupCatg, groupCatg.getParentId());
            if (selectedItem != null) {
                boolean existed = shiftOrgExisted(groupCatg, selectedItem.getShiftOrg());
                if (existed)
                    return;

                if (selectedItem.getShiftOrg() != null) {
                    selectedItem.getShiftOrg().add(ShiftConfigOrgDto.builder()
                            .groupId(groupCatg.getGroupId())
                            .groupCode(groupCatg.getGroupCode())
                            .groupName(groupCatg.getGroupName())
                            .orgId(depCatg.getGroupId())
                            .orgCode(depCatg.getGroupCode())
                            .orgName(depCatg.getGroupName())
                            .status("1")
                            .build());
                } else {
                    List<ShiftConfigOrgDto> lstShiftOrg = new ArrayList<>();
                    lstShiftOrg.add(ShiftConfigOrgDto.builder()
                            .groupId(groupCatg.getGroupId())
                            .groupCode(groupCatg.getGroupCode())
                            .groupName(groupCatg.getGroupName())
                            .orgId(depCatg.getGroupId())
                            .orgCode(depCatg.getGroupCode())
                            .orgName(depCatg.getGroupName())
                            .status("1")
                            .build());
                    selectedItem.setShiftOrg(lstShiftOrg);
                }
                selectedItem.setDepId(null);
                selectedItem.setGroupId(null);
                lstGroupCatgChild = new ArrayList<>();
            }
        }
    }

    private boolean shiftOrgExisted(GroupCatg groupCatg, List<ShiftConfigOrgDto> shiftOrg) {
        if (shiftOrg != null) {
            for (ShiftConfigOrgDto org : shiftOrg) {
                if (groupCatg.getGroupId().equals(org.getGroupId())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void deleteShiftOrg(ShiftConfigOrgDto item) {
        if (selectedItem != null) {
            if (selectedItem.getShiftOrg() != null) {
                int index = -1;
                for (int i = 0; i < selectedItem.getShiftOrg().size(); i++) {
                    ShiftConfigOrgDto org = selectedItem.getShiftOrg().get(i);
                    if (item.getGroupId().equals(org.getGroupId())) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0) {
                    selectedItem.getShiftOrg().remove(index);
                }
            }
        }
    }

    public void changeMode(ShiftConfigDto item) {
        if (item != null) {
            updateMode = true;
            selectedItem = getDetailSelected(item.getAutoid());
            ;
            GroupCatg groupCatg = groupUserClient.findGroupById(lstAllGroupCatg, selectedItem.getGroupId());
            if (groupCatg != null) {
                lstGroupCatgChild = Arrays.asList(groupCatg);
            }
            buildDetail();
        } else {
            updateMode = false;
            selectedItem = new ShiftConfigDto();
            buildDetail();
        }
    }

    public boolean isViewDetail() {
        return false;
    }


    private void buildDetail() {
        if (selectedItem != null) {
            Set<String> sOverTime = new HashSet<>();
            for (int i = 1; i <= 7; i++) {
                ShiftConfigDetailDto dayOfWeek = getDayOfWeekInList(i, selectedItem.getOshiftDetail());
                if (dayOfWeek == null) {
                    dayOfWeek = ShiftConfigDetailDto.builder().day("" + i).dayName(Utils.dayOfWeekName(i))
                            .shiftId(selectedItem.getAutoid() != null ? selectedItem.getAutoid() : null)
                            .shiftCode(selectedItem.getCode() != null ? selectedItem.getCode() : null)
                            .status("1")
                            .isOverTime(false)
                            .build();
                    dayOfWeek = timeConvertToLT(dayOfWeek);
                    if (CollectionUtils.isEmpty(selectedItem.getOshiftDetail())) {
                        List<ShiftConfigDetailDto> oshiftDetail = new ArrayList<>();
                        selectedItem.setOshiftDetail(oshiftDetail);
                    }
                    selectedItem.getOshiftDetail().add(dayOfWeek);
                } else {
                    dayOfWeek = timeConvertToLT(dayOfWeek);
                    sOverTime.add(dayOfWeek.getDay());
                }
            }

            for (int i = 1; i <= 7; i++) {
                ShiftConfigDetailDto dayOfWeek = getDayOfWeekInList(i, selectedItem.getShiftDetail());
                if (dayOfWeek == null) {
                    dayOfWeek = ShiftConfigDetailDto.builder().day("" + i).dayName(Utils.dayOfWeekName(i))
                            .shiftId(selectedItem.getAutoid() != null ? selectedItem.getAutoid() : null)
                            .shiftCode(selectedItem.getCode() != null ? selectedItem.getCode() : null)
                            .status("1")
                            .build();
                    dayOfWeek = timeConvertToLT(dayOfWeek);
                    if (CollectionUtils.isEmpty(selectedItem.getShiftDetail())) {
                        List<ShiftConfigDetailDto> shiftDetail = new ArrayList<>();
                        selectedItem.setShiftDetail(shiftDetail);
                    }
                    selectedItem.getShiftDetail().add(dayOfWeek);
                } else {
                    dayOfWeek = timeConvertToLT(dayOfWeek);
                    if (sOverTime.contains(dayOfWeek.getDay())) {
                        dayOfWeek.setIsOverTime(true);
                    }
                }
            }

            selectedItem.setShiftDetail(selectedItem.getShiftDetail().stream()
                    .sorted(Comparator.comparing(ShiftConfigDetailDto::getDay))
                    .collect(Collectors.toList()));

            selectedItem.setOshiftDetail(selectedItem.getOshiftDetail().stream()
                    .sorted(Comparator.comparing(ShiftConfigDetailDto::getDay))
                    .collect(Collectors.toList()));
        }
        log.info("selectedItem: ", Utils.objToJson(selectedItem));
    }

    private ShiftConfigDetailDto timeConvertToLT(ShiftConfigDetailDto dayOfWeek) {
        if (dayOfWeek != null) {
            dayOfWeek.setWorkingStartTimeLT(!StringUtils.isEmpty(dayOfWeek.getWorkingStartTime()) ? LocalTime.parse(dayOfWeek.getWorkingStartTime()) : null);
            dayOfWeek.setWorkingEndTimeLT(!StringUtils.isEmpty(dayOfWeek.getWorkingEndTime()) ? LocalTime.parse(dayOfWeek.getWorkingEndTime()) : null);
            dayOfWeek.setBreakStartTimeLT(!StringUtils.isEmpty(dayOfWeek.getBreakStartTime()) ? LocalTime.parse(dayOfWeek.getBreakStartTime()) : null);
            dayOfWeek.setBreakEndTimeLT(!StringUtils.isEmpty(dayOfWeek.getBreakEndTime()) ? LocalTime.parse(dayOfWeek.getBreakEndTime()) : null);
            dayOfWeek.setShiftMiddleStartLT(!StringUtils.isEmpty(dayOfWeek.getShiftMiddleStart()) ? LocalTime.parse(dayOfWeek.getShiftMiddleStart()) : null);
            dayOfWeek.setShiftMiddleEndLT(!StringUtils.isEmpty(dayOfWeek.getShiftMiddleEnd()) ? LocalTime.parse(dayOfWeek.getShiftMiddleEnd()) : null);
            return dayOfWeek;
        }
        return null;
    }

    private ShiftConfigDetailDto timeConvertLTToStr(ShiftConfigDetailDto dayOfWeek) {
        if (dayOfWeek != null) {
            dayOfWeek.setWorkingStartTime(!StringUtils.isEmpty(dayOfWeek.getWorkingStartTimeLT()) ? dayOfWeek.getWorkingStartTimeLT().toString() : null);
            dayOfWeek.setWorkingEndTime(!StringUtils.isEmpty(dayOfWeek.getWorkingEndTimeLT()) ? dayOfWeek.getWorkingEndTimeLT().toString() : null);
            dayOfWeek.setBreakStartTime(!StringUtils.isEmpty(dayOfWeek.getBreakStartTimeLT()) ? dayOfWeek.getBreakStartTimeLT().toString() : null);
            dayOfWeek.setBreakEndTime(!StringUtils.isEmpty(dayOfWeek.getBreakEndTimeLT()) ? dayOfWeek.getBreakEndTimeLT().toString() : null);
            dayOfWeek.setShiftMiddleStart(!StringUtils.isEmpty(dayOfWeek.getShiftMiddleStartLT()) ? dayOfWeek.getShiftMiddleStartLT().toString() : null);
            dayOfWeek.setShiftMiddleEnd(!StringUtils.isEmpty(dayOfWeek.getShiftMiddleEndLT()) ? dayOfWeek.getShiftMiddleEndLT().toString() : null);
            return dayOfWeek;
        }
        return null;
    }

    private ShiftConfigDto cleanDetailTimeEmpty(ShiftConfigDto shift) {
        if (shift != null) {

            ShiftConfigDto insertData = ShiftConfigDto.builder()
                    .autoid(shift.getAutoid())
                    .code(shift.getCode())
                    .name(shift.getName())
                    .status(shift.getStatus())
                    .workingDayType(shift.getWorkingDayType())
                    .shiftOrg(shift.getShiftOrg())
                    .build();

            if (shift.getShiftDetail() != null) {
                List<ShiftConfigDetailDto> lstDetail = new ArrayList<>();
                shift.getShiftDetail().forEach(item -> {
                    if (!StringUtils.isEmpty(item.getWorkingStartTime())
                            || !StringUtils.isEmpty(item.getWorkingEndTime())
                            || !StringUtils.isEmpty(item.getShiftMiddleStart())
                            || !StringUtils.isEmpty(item.getShiftMiddleEnd())
                            || !StringUtils.isEmpty(item.getBreakStartTime())
                            || !StringUtils.isEmpty(item.getBreakEndTime())) {
                        lstDetail.add(item);
                    }
                });
                insertData.setShiftDetail(lstDetail);
            }

            if (shift.getOshiftDetail() != null) {
                List<ShiftConfigDetailDto> lstODetail = new ArrayList<>();
                shift.getOshiftDetail().forEach(item -> {
                    if (!StringUtils.isEmpty(item.getWorkingStartTime())
                            || !StringUtils.isEmpty(item.getWorkingEndTime())
                            || !StringUtils.isEmpty(item.getShiftMiddleStart())
                            || !StringUtils.isEmpty(item.getShiftMiddleEnd())
                            || !StringUtils.isEmpty(item.getBreakStartTime())
                            || !StringUtils.isEmpty(item.getBreakEndTime())) {
                        lstODetail.add(item);
                    }
                });
                insertData.setOshiftDetail(lstODetail);
            }
            return insertData;
        }
        return null;
    }

    private ShiftConfigDto setDetailTimeError(ShiftConfigDto shift, List<ShiftConfigDetailDto> lstError) {
        if (shift != null && lstError != null) {
            if (shift.getShiftDetail() != null) {
                List<ShiftConfigDetailDto> lstDetail = new ArrayList<>();
                shift.getShiftDetail().forEach(item -> {
                    lstError.forEach(error -> {
                        if (item.getDay().equals(error.getDay()) && !error.isOverTimeError()) {
                            item.setError("error");
                            item.setMessage(error.getMessage());
                        }
                    });
                });
            }
            if (shift.getOshiftDetail() != null) {
                List<ShiftConfigDetailDto> lstDetail = new ArrayList<>();
                shift.getOshiftDetail().forEach(item -> {
                    lstError.forEach(error -> {
                        if (item.getDay().equals(error.getDay()) && error.isOverTimeError()) {
                            item.setError("error");
                            item.setMessage(error.getMessage());
                        }
                    });
                });
            }
        }
        return shift;
    }

    private ResponseListData<ShiftConfigDetailDto> validateTimeDetail(ShiftConfigDto shift, boolean update) {
        ResponseListData<ShiftConfigDetailDto> validate = new ResponseListData<>();
        if (shift != null) {

            if (update) {
                ShiftConfigDto shiftTemp = shiftConfigClient.shiftConfigDetail(shift.getCode());
                if (shiftTemp != null && !shiftTemp.getAutoid().equals(shift.getAutoid())) {
                    validate.setMessage("Mã ca làm việc đã tồn tại");
                    return validate;
                }
            } else {
                ShiftConfigDto shiftTemp = shiftConfigClient.shiftConfigDetail(shift.getCode());
                if (shiftTemp != null) {
                    validate.setMessage("Mã ca làm việc đã tồn tại");
                    return validate;
                }
            }

            if (CollectionUtils.isEmpty(shift.getShiftDetail())) {
                validate.setMessage("Chưa khai báo thời gian");
                return validate;
            }

            if (CollectionUtils.isEmpty(shift.getShiftOrg())) {
                validate.setMessage("Chưa chọn phòng ban áp dụng");
                return validate;
            }


            List<ShiftConfigDetailDto> lstError = new ArrayList<>();
            boolean validTimeDetail = true;
            Set<String> setOverTime = new HashSet<>();
            if (shift.getShiftDetail() != null) {

                for (ShiftConfigDetailDto item : shift.getShiftDetail()) {

                    if (item.getIsOverTime()) {
                        setOverTime.add(item.getDay());
                    }
                    long start = 0;
                    long end = 0;
                    if (!StringUtils.isEmpty(item.getWorkingStartTime()) && !StringUtils.isEmpty(item.getWorkingEndTime())) {
                        start = Utils.strTimeToLong(item.getWorkingStartTime());
                        end = Utils.strTimeToLong(item.getWorkingEndTime());
                        if (start >= end) {
                            validTimeDetail = false;
                            item.setOverTimeError(false);
                            lstError.add(item);
                            continue;
                        }
                    }

                    if (!StringUtils.isEmpty(item.getShiftMiddleStart()) && !StringUtils.isEmpty(item.getShiftMiddleEnd())) {
                        start = Utils.strTimeToLong(item.getShiftMiddleStart());
                        end = Utils.strTimeToLong(item.getShiftMiddleEnd());
                        if (start >= end) {
                            validTimeDetail = false;
                            item.setOverTimeError(false);
                            lstError.add(item);
                            continue;
                        }
                    }
                }
            }

            boolean validOTimeDetail = true;
            if (shift.getOshiftDetail() != null) {
                for (ShiftConfigDetailDto item : shift.getOshiftDetail()) {
                    setOverTime.remove(item.getDay());

                    long start = 0;
                    long end = 0;
                    if (!StringUtils.isEmpty(item.getWorkingStartTime()) && !StringUtils.isEmpty(item.getWorkingEndTime())) {
                        start = Utils.strTimeToLong(item.getWorkingStartTime());
                        end = Utils.strTimeToLong(item.getWorkingEndTime());
                        if (start >= end) {
                            validOTimeDetail = false;
                            item.setOverTimeError(true);
                            lstError.add(item);
                            continue;
                        }
                    }

                    if (!StringUtils.isEmpty(item.getShiftMiddleStart()) && !StringUtils.isEmpty(item.getShiftMiddleEnd())) {
                        start = Utils.strTimeToLong(item.getShiftMiddleStart());
                        end = Utils.strTimeToLong(item.getShiftMiddleEnd());
                        if (start >= end) {
                            validOTimeDetail = false;
                            item.setOverTimeError(true);
                            lstError.add(item);
                            continue;
                        }
                    }
                    if (!StringUtils.isEmpty(item.getBreakStartTime()) && !StringUtils.isEmpty(item.getBreakEndTime())) {
                        start = Utils.strTimeToLong(item.getBreakStartTime());
                        end = Utils.strTimeToLong(item.getBreakEndTime());
                        if (start >= end) {
                            validOTimeDetail = false;
                            item.setOverTimeError(true);
                            lstError.add(item);
                            continue;
                        }
                    }
                }
            }
            if (!validTimeDetail || !validOTimeDetail) {
                validate.setMessage("Khoảng thời gian không hợp lệ");
                validate.setData(lstError);
                return validate;
            }

            if (!setOverTime.isEmpty()) {
                validate.setMessage("Overtime chưa khai đủ");
                setOverTime.forEach(day -> {
                    selectedItem.getOshiftDetail().forEach(itemDetail -> {
                        if (day.equals(itemDetail.getDay())) {
                            itemDetail.setOverTimeError(true);
                            lstError.add(itemDetail);
                        }
                    });
                });
                validate.setData(lstError);
                return validate;
            }
        }
        return null;
    }
}
