package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.util.ConfProperties;
import com.lvt.khvip.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ShiftClient implements Serializable {

	private static String url = ConfProperties.getProperty("backend.enpoint");
	
	private ObjectMapper mapper;

	public ShiftClient() {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public ShiftCatg findShiftCatgById(Integer id) {
		if (id != null) {
			List<ShiftCatg> lstShiftCatg = listShiftCatg();
			for (ShiftCatg item : lstShiftCatg) {
				if (id.equals(item.getAutoid())) {
					return item;
				}
			}
		}
		return null;
	}

	public ShiftCatg findShiftCatgByCode(String code) {
		if (code != null) {
			List<ShiftCatg> lstShiftCatg = listShiftCatg();
			for (ShiftCatg item : lstShiftCatg) {
				if (code.equals(item.getCode())) {
					return item;
				}
			}
		}
		return null;
	}

	public List<ShiftCatg> listShiftCatg() {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpGet http = new HttpGet(url.concat("/shifts"));
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			
			ResponseListData<ShiftCatg> responseListData = mapper.readValue(jsonResponse,
					new TypeReference<ResponseListData<ShiftCatg>>() {
					});
			return responseListData.getData();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public ResponseListData<ShiftListData> listAllShift(Integer depId, String peopleId, String fullName, Integer groupId,
			Integer pageIndex, Integer pageSize) {
		try {
			log.info("listAllShift: {} - {} - {} - {} - {}", peopleId, fullName, groupId, pageIndex, pageSize);
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/shift-peoples?"));
			urlBuilder.append("&page=" + pageIndex).append("&limit=" + pageSize);
			if (peopleId != null) {
				urlBuilder.append("&peopleId=" + peopleId);
			}
			if (fullName != null) {
				urlBuilder.append("&fullName=" + fullName);
			}
            if (groupId != null) {
                urlBuilder.append("&depId=" + depId);
            }
			if (groupId != null) {
				urlBuilder.append("&gId=" + groupId);
			}
			HttpGet http = new HttpGet(urlBuilder.toString());
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			
			ResponseListData<ShiftListData> responseListData = mapper.readValue(jsonResponse,
					new TypeReference<ResponseListData<ShiftListData>>() {
					});
			if (responseListData != null && responseListData.getData() != null) {
				List<ShiftListData> data = new ArrayList<>();
				responseListData.getData().forEach(item -> {
					if (item.getGroupId() == null) {
						item.setGroupId(item.getGid());
					}

					if(item.getDepId() == null && item.getGroupId() != null){
						item.setDepId(item.getGroupId());
						item.setDepName(item.getGroupName());
						item.setGroupId(null);
						item.setGroupName(null);
					}

					data.add(item);
				});
			}
			log.info("listAllShift: {} - {} - {} - {} - {} > {}", peopleId, fullName, groupId, pageIndex, pageSize,
					responseListData.getData());
			return responseListData;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public ProfileShift getProfileShift(String peopleId) {
		try {
			log.info("getProfileShift: {} ", peopleId);
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/shift-people/shift?people_id="+peopleId));
//			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/shift-people/shift?people_id=" + peopleId));
			HttpGet http = new HttpGet(urlBuilder.toString());
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			if(!StringUtils.isEmpty(jsonResponse)) {
				ProfileShift responseListData = mapper.readValue(jsonResponse,
						new TypeReference<ProfileShift>() {
						});
				return responseListData;
			} else
				return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public boolean delete(String peopleId) {
		try {
			ShiftData insertData = ShiftData.builder().peopleId(peopleId).build();

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost http = new HttpPost(url.concat("/shift-people/delete"));


			String jsonData = mapper.writeValueAsString(insertData);
			log.info("delete jsonRequest {}", jsonData);
			StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
			entity.setContentType("application/json");
			http.setHeader("Content-type", "application/json");
			http.setEntity(entity);

			HttpResponse response = client.execute(http);

			HttpEntity entityResponse = response.getEntity();

			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("delete jsonResponse {}", jsonResponse);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}

	public ShiftPeopleDetail getShiftPeopleDetail(String peopleId) {
		try {
			log.info("getProfileShift: {} ", peopleId);
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/shift-people/detail?peopleId="+peopleId));
			HttpGet http = new HttpGet(urlBuilder.toString());
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			if(!StringUtils.isEmpty(jsonResponse)) {
				ShiftPeopleDetail responseListData = mapper.readValue(jsonResponse,
						new TypeReference<ShiftPeopleDetail>() {
						});
				return responseListData;
			} else
				return null;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public ShiftListData create(ShiftListData shift) {
		try {
			ShiftData insertData = ShiftData.builder().autoid(shift.getAutoid()).peopleId(shift.getPeopleId())
					.startDate(shift.getStartDate()).expireDate(shift.getExpireDate()).shiftId(shift.getShiftId())
					.shiftTarget(shift.getShiftTarget()).gid(shift.getGid()).groupId(shift.getGroupId()).build();

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost http = new HttpPost(url.concat("/shift-people"));

			
			String jsonData = mapper.writeValueAsString(insertData);
			log.info("create jsonRequest {}", jsonData);
			StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
			entity.setContentType("application/json");
			http.setHeader("Content-type", "application/json");
			http.setEntity(entity);

			HttpResponse response = client.execute(http);

			HttpEntity entityResponse = response.getEntity();

			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("create jsonResponse {}", jsonResponse);

			ShiftListData shiftListData = mapper.readValue(jsonResponse, new TypeReference<ShiftListData>() {
			});
			return shiftListData;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public ImportData<ShiftListData> importShift(String importBy, String attachment, String title, List<ShiftListData> shifts) {
		try {
			List<ShiftData> lstInsertData = new ArrayList<>();
			shifts.forEach(shift -> {
				lstInsertData.add(ShiftData.builder().autoid(shift.getAutoid()).peopleId(shift.getPeopleId())
						.startDate(shift.getStartDate()).expireDate(shift.getExpireDate()).shiftId(shift.getShiftId())
						.shiftTarget(shift.getShiftTarget()).gid(shift.getGid()).groupId(shift.getGroupId()).build());
			});

			ImportData<ShiftData> shiftImportData = new ImportData();
			shiftImportData.setObjectImport(lstInsertData);
			shiftImportData.setImportBy(importBy);
			shiftImportData.setAttachment(attachment);
			shiftImportData.setTitle(title);

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost http = new HttpPost(url.concat("/shift-people/import-excel"));

			
			String jsonData = mapper.writeValueAsString(shiftImportData);
			log.info("create jsonRequest {}", jsonData);
			StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
			entity.setContentType("application/json");
			http.setHeader("Content-type", "application/json");
			http.setEntity(entity);

			HttpResponse response = client.execute(http);

			HttpEntity entityResponse = response.getEntity();

			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("create jsonResponse {}", jsonResponse);

			ImportData<ShiftListData> shiftImportDataRs = mapper.readValue(jsonResponse,
					new TypeReference<ImportData<ShiftListData>>() {
					});
			return shiftImportDataRs;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public ShiftListData update(ShiftListData shift) {
		try {
			ShiftData updateData = ShiftData.builder().autoid(shift.getAutoid()).peopleId(shift.getPeopleId())
					.startDate(shift.getStartDate()).expireDate(shift.getExpireDate()).shiftId(shift.getShiftId())
					.shiftTarget(shift.getShiftTarget()).gid(shift.getGid()).groupId(shift.getGroupId()).build();

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPut http = new HttpPut(url.concat("/shift-people"));

			
			String jsonData = mapper.writeValueAsString(updateData);
			log.info("update jsonRequest {}", jsonData);
			StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
			entity.setContentType("application/json");
			http.setHeader("Content-type", "application/json");
			http.setEntity(entity);

			HttpResponse response = client.execute(http);

			HttpEntity entityResponse = response.getEntity();

			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("update jsonResponse {}", jsonResponse);

			ShiftListData shiftListData = mapper.readValue(jsonResponse, new TypeReference<ShiftListData>() {
			});
			return shiftListData;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public boolean delete(long autoId) {
		try {

			CloseableHttpClient client = HttpClients.createDefault();
			HttpDelete http = new HttpDelete(url.concat("/shift-people?autoid=" + autoId));
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entityResponse = response.getEntity();
			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("delete {}", autoId);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return false;
		}
	}
}
