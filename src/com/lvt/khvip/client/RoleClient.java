package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.author.dto.Roles;
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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class RoleClient implements Serializable {

	private static String url = ConfProperties.getProperty("backend.enpoint");

	private ObjectMapper mapper;

	public RoleClient() {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public List<Roles> listRole() {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/roles"));
			HttpGet http = new HttpGet(urlBuilder.toString());
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			ResponseListData<Roles> rs = mapper.readValue(jsonResponse, new TypeReference<ResponseListData<Roles>>() {
			});
			return rs.getData();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}

	public List<Roles> listRole(Boolean approveable) {
		List<Roles> lstRole = listRole();
		if (approveable != null) {
			return lstRole.stream()
					.filter(item -> item.getApproveable() == approveable)
					.collect(Collectors.toList());
		} else {
			return lstRole;
		}
	}

	public ResponseData<Roles> create(Roles role) {
		try {

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost http = new HttpPost(url.concat("/role"));

			ObjectMapper mapper = new ObjectMapper();
			String jsonData = mapper.writeValueAsString(role);
			log.info("create jsonRequest {}", jsonData);
			StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
			entity.setContentType("application/json");
			http.setHeader("Content-type", "application/json");
			http.setEntity(entity);

			HttpResponse response = client.execute(http);
			HttpEntity entityResponse = response.getEntity();
			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("create jsonResponse {}", jsonResponse);
			if (response.getStatusLine().getStatusCode() == 200) {
				Roles rs = mapper.readValue(jsonResponse, new TypeReference<Roles>() {
				});
				ResponseData<Roles> responseData = new ResponseData<>();
				responseData.setData(rs);
				return responseData;
			} else {
				ResponseData<Roles> responseData = mapper.readValue(jsonResponse,
						new TypeReference<ResponseData<Roles>>() {
						});
				return responseData;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ResponseData<Roles> responseData = new ResponseData<>();
			responseData.setMessage(e.getMessage());
			return responseData;
		}
	}

	public ResponseData<Roles> update(Roles role) {
		try {

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPut http = new HttpPut(url.concat("/role"));

			ObjectMapper mapper = new ObjectMapper();
			String jsonData = mapper.writeValueAsString(role);
			log.info("update jsonRequest {}", jsonData);
			StringEntity entity = new StringEntity(jsonData, StandardCharsets.UTF_8);
			entity.setContentType("application/json");
			http.setHeader("Content-type", "application/json");
			http.setEntity(entity);
			HttpResponse response = client.execute(http);
			HttpEntity entityResponse = response.getEntity();
			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("update jsonResponse {}", jsonResponse);
			if (response.getStatusLine().getStatusCode() == 200) {
				Roles rs = mapper.readValue(jsonResponse, new TypeReference<Roles>() {
				});
				ResponseData<Roles> responseData = new ResponseData<>();
				responseData.setData(rs);
				return responseData;
			} else {
				ResponseData<Roles> responseData = mapper.readValue(jsonResponse,
						new TypeReference<ResponseData<Roles>>() {
						});
				return responseData;
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			ResponseData<Roles> responseData = new ResponseData<>();
			responseData.setMessage(e.getMessage());
			return responseData;
		}
	}

	public boolean delete(long autoId) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpDelete http = new HttpDelete(url.concat("/role?autoid=" + autoId));
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			if (response.getStatusLine().getStatusCode() == 200) {
				return true;
			} else {
				HttpEntity entityResponse = response.getEntity();
				String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
				log.info("delete {}", jsonResponse);
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
