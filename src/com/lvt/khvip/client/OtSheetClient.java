package com.lvt.khvip.client;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.entity.*;
import com.lvt.khvip.util.BackendUtils;
import com.lvt.khvip.util.StringUtils;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class OtSheetClient implements Serializable {

	private ObjectMapper mapper;

	public OtSheetClient(){
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public ImportExcelDto<OtSheetData> importExcel(ImportExcelDto importExcelDto){
		try {
			String response =  BackendUtils.sendPost(Constants.BackendApis.OTSHEET_IMPORT_EXCEL, importExcelDto, "POST");

			ImportExcelDto<OtSheetData> rs = mapper.readValue(response,
					new TypeReference<ImportExcelDto<OtSheetData>>() {
					});

			return rs;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ImportExcelDto updateExcel(ImportExcelDto importExcelDto){
		String response =  BackendUtils.sendPost(Constants.BackendApis.TIMEKEEPINGSHEET_IMPORT_EXCEL, importExcelDto, "PUT");

		return (ImportExcelDto) BackendUtils.convertJsonToObj(response, ImportExcelDto.class);
	}

	public void approve(ApproveTimeKeeping approveTimeKeeping){
		String response =  BackendUtils.sendPost(Constants.BackendApis.APPROVE_OT, approveTimeKeeping, "POST");
	}

	public ResponseListData<OtSheetData> getAllListOtSheet(OtSheetData condition, Integer page, Integer limit){
		try {
			StringBuilder param = new StringBuilder("&order_field=createdAt&order_type=false");

			if (condition != null) {
				if (!StringUtils.isEmpty(condition.getFullName())) {
					String fullName = URLEncoder.encode(condition.getFullName(), "UTF-8");

					param.append("&fullName=" + fullName);
				}

				if (!StringUtils.isEmpty(condition.getPeopleId())) {
					param.append("&peopleId=" + condition.getPeopleId());
				}

				if (!StringUtils.isEmpty(condition.getGroupName())) {
					String groupName = URLEncoder.encode(condition.getGroupName(), "UTF-8");

					param.append("&groupName=" + groupName);
				}

				if (!StringUtils.isEmpty(condition.getGroupId())) {
					param.append("&groupId=" + condition.getGroupId());
				}

				if (!StringUtils.isEmpty(condition.getCreateAtForSearch())) {
					//createdAtStr = simpleDateFormat.format(condition.getCreatedAt());
					param.append(condition.getCreateAtForSearch());
				}

				if (!StringUtils.isEmpty(condition.getState())) {
					param.append("&state=" + condition.getState());
				}

				if (!StringUtils.isEmpty(condition.getApprovedBy())) {
					param.append("&approvedBy=" + condition.getApprovedBy());
				}

				if (!StringUtils.isEmpty(condition.getApprovalByLevel2())) {
					param.append("&approvalByLevel2=" + condition.getApprovalByLevel2());
				}
			}

			if (page != null) {
				param.append("&page=" + page);
			}

			if (limit != null) {
				param.append("&limit=" + limit);
			}

			String response = BackendUtils.sendGET(Constants.BackendApis.OTSHEET_GET_ALL, param.toString());
			ResponseListData<OtSheetData> rs = (ResponseListData<OtSheetData>) BackendUtils
					.convertJsonToObj(response, ResponseListData.class);
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ResponseListData<OtSheetData> getAllListOtSheetCustom(OtSheetData condition) {
		try {
			String response = BackendUtils.sendPost(Constants.BackendApis.OTSHEET_CUSTOM, condition, "POST");
			ResponseListData<OtSheetData> rs = (ResponseListData<OtSheetData>) BackendUtils
					.convertJsonToObj(response, ResponseListData.class);
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public List<String> getListManager(Integer groupId){
		if (ObjectUtils.isEmpty(groupId)){
			return new ArrayList();
		}

		StringBuilder param = new StringBuilder();
		param.append("&autoid="+groupId);

		String response = BackendUtils.sendGET(Constants.BackendApis.GROUP, param.toString());
		Groups group = (Groups) BackendUtils.convertJsonToObj(response, Groups.class);
		List<String> listManager = new ArrayList();
		if(!StringUtils.isEmpty(group.getManager())){
			listManager.add(group.getManager());
		}

		return listManager;
	}

	public TimekeepingSheetData updateTimeKeepingSheet(TimekeepingSheetData TimekeepingSheetData){
		String response =  BackendUtils.sendPost(Constants.BackendApis.TIMEKEEPINGSHEET, TimekeepingSheetData, "PUT");
				
		return (TimekeepingSheetData) BackendUtils.convertJsonToObj(response, TimekeepingSheetData.class);
	}

	public OtSheetData updateOtSheet(OtSheetData TimekeepingSheetData){
		String response =  BackendUtils.sendPost(Constants.BackendApis.OTSHEET, TimekeepingSheetData, "PUT");

		return (OtSheetData) BackendUtils.convertJsonToObj(response, OtSheetData.class);
	}

	public OtSheetData addNewOtSheet(OtSheetData otSheetData){
		String response =  BackendUtils.sendPost(Constants.BackendApis.OTSHEET, otSheetData, "POST");

		return (OtSheetData) BackendUtils.convertJsonToObj(response, OtSheetData.class);
	}

	public OtSheetData editTimeKeepingSheet(OtSheetData otSheetData){
		String response =  BackendUtils.sendPost(Constants.BackendApis.OTSHEET, otSheetData, "PUT");

		return (OtSheetData) BackendUtils.convertJsonToObj(response, OtSheetData.class);
	}

	public TimekeepingSheetData updateTimeKeepingSheetExcel(TimekeepingSheetExcel timekeepingSheetData){
		String response =  BackendUtils.sendPost(Constants.BackendApis.TIMEKEEPINGSHEET, timekeepingSheetData, "PUT");

		return (TimekeepingSheetData) BackendUtils.convertJsonToObj(response, TimekeepingSheetExcel.class);
	}

	public OtSheetData getOtSheet(OtSheetData condition){
		StringBuilder param = new StringBuilder("?1=1");

		if (condition != null) {
			if (!ObjectUtils.isEmpty(condition.getAutoid())) {

				param.append("&autoid=" + condition.getAutoid());
			}
		}

		String response = BackendUtils.sendGET(Constants.BackendApis.OTSHEET, param.toString());
		OtSheetData rs = (OtSheetData) BackendUtils
				.convertJsonToObj(response, OtSheetData.class);

		return rs;
	}
}
