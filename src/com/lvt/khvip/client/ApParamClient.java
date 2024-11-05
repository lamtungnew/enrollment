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

@Slf4j
public class ApParamClient implements Serializable {

	private static String url = ConfProperties.getProperty("backend.enpoint");
	
	private ObjectMapper mapper;

	public ApParamClient() {
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}
	
	public List<ApParamDto> listApParam(String group) {
		try {
			CloseableHttpClient client = HttpClients.createDefault();
			StringBuilder urlBuilder = new StringBuilder(this.url.concat("/apparams?"));
			if (group != null) {
				urlBuilder.append("group=" + group);
			}
			HttpGet http = new HttpGet(urlBuilder.toString());
			http.setHeader("Content-type", "application/json");
			HttpResponse response = client.execute(http);
			HttpEntity entity = response.getEntity();
			String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
			ResponseListData<ApParamDto> rs = mapper.readValue(jsonResponse, new TypeReference<ResponseListData<ApParamDto>>() {
			});
			return rs.getData();
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
}
