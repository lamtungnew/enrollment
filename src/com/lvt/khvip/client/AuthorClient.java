package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.author.dto.ModuleRoleActionDto;
import com.lvt.khvip.client.author.dto.PermissionListDto;
import com.lvt.khvip.client.author.dto.UserPermissionDto;
import com.lvt.khvip.client.dto.*;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AuthorClient implements Serializable {

	private static String url = ConfProperties.getProperty("backend.enpoint");

	private ObjectMapper mapper;

	public AuthorClient() {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public List<UserPermissionDto> getUserPermissions(String userName) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/author/user/permissions?"));
			if (userName != null) {
				urlBuilder.append("userName=" + userName);
			}
			HttpGet http = new HttpGet(urlBuilder.toString());
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			List<UserPermissionDto> rs = mapper.readValue(jsonResponse, new TypeReference<List<UserPermissionDto>>() {
			});
			return rs;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public PermissionListDto getPermissions() {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/author/config/permissions"));
			HttpGet http = new HttpGet(urlBuilder.toString());
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			ObjectMapper mapper = new ObjectMapper();
			PermissionListDto rs = mapper.readValue(jsonResponse, new TypeReference<PermissionListDto>() {
			});
			return rs;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public boolean updatePermissions(List<ModuleRoleActionDto> moduleRoleActions) {
		try {

			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/author/config/permissions"));
			HttpPost http = new HttpPost(urlBuilder.toString());
			ObjectMapper mapper = new ObjectMapper();
			String jsonData = mapper.writeValueAsString(moduleRoleActions);
			log.info("updatePermissions jsonRequest {}", jsonData);
			StringEntity entity = new StringEntity(jsonData);
			entity.setContentType("application/json");
			http.setHeader("Content-type", "application/json");
			http.setEntity(entity);

			HttpResponse response = client.execute(http);

			HttpEntity entityResponse = response.getEntity();

			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("updatePermissions jsonResponse {}", jsonResponse);

			Boolean rs = mapper.readValue(jsonResponse, new TypeReference<Boolean>() {
			});
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
