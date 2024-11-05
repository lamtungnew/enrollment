package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.util.ConfProperties;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GroupUserClient implements Serializable {

	private static String url = ConfProperties.getProperty("backend.enpoint");
	
	private ObjectMapper mapper;

	public GroupUserClient() {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public List<GroupCatg> listGroupCatg() {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpGet http = new HttpGet(url.concat("/groups"));
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			ResponseListData<GroupCatg> responseListData = mapper.readValue(jsonResponse,
					new TypeReference<ResponseListData<GroupCatg>>() {
					});
			return responseListData.getData();
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public List<GroupCatg> listGroupCatg(Integer parentId) {
		List<GroupCatg> listRs = new ArrayList<>();
		if (parentId != null) {
			List<GroupCatg> listAll = listGroupCatg();
			if (parentId == -1) {
				listAll.forEach(item -> {
					if (item.getRootId() == null || item.getRootId() == -1
							|| (item.getParentId() == null || item.getParentId() == 0))
						listRs.add(item);
				});
			} else {
				listAll.forEach(item -> {
					if (parentId.equals(item.getParentId()))
						listRs.add(item);
				});
			}
		}
		return listRs;
	}

	public List<GroupCatg> listGroupCatg(List<GroupCatg> lstGroupIn, Integer parentId) {
		List<GroupCatg> listRs = new ArrayList<>();
		if (parentId != null) {
			List<GroupCatg> listAll = lstGroupIn != null ? lstGroupIn : listGroupCatg();
			if (parentId == -1) {
				listAll.forEach(item -> {
					if (item.getRootId() == null || item.getRootId() == -1
							|| (item.getParentId() == null || item.getParentId() == 0))
						listRs.add(item);
				});
			} else {
				listAll.forEach(item -> {
					if (parentId.equals(item.getParentId()))
						listRs.add(item);
				});
			}
		}
		return listRs;
	}

	public GroupCatg findGroupById(Integer id) {
		if (id != null) {
			List<GroupCatg> lstGroup = listGroupCatg();
			for (GroupCatg item : lstGroup) {
				if (id.equals(item.getGroupId())) {
					return item;
				}
			}
		}
		return null;
	}

	public GroupCatg findGroupById(List<GroupCatg> lstGroupIn, Integer id) {
		if (id != null) {
			List<GroupCatg> lstGroup = lstGroupIn != null ? lstGroupIn : listGroupCatg();
			for (GroupCatg item : lstGroup) {
				if (id.equals(item.getGroupId())) {
					return item;
				}
			}
		}
		return null;
	}

	public GroupCatg findGroupByCode(String code) {
		if (code != null) {
			List<GroupCatg> lstGroup = listGroupCatg();
			for (GroupCatg item : lstGroup) {
				if (code.equals(item.getGroupCode())) {
					return item;
				}
			}
		}
		return null;
	}
	
	public GroupCatg findGroupByCode(List<GroupCatg> lstGroupIn, String code) {
		if (code != null) {
			List<GroupCatg> lstGroup = lstGroupIn != null ? lstGroupIn : listGroupCatg();
			for (GroupCatg item : lstGroup) {
				if (code.equals(item.getGroupCode())) {
					return item;
				}
			}
		}
		return null;
	}
}
