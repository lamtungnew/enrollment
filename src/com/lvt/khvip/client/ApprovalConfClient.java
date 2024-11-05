package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.dto.ApprovalConfDto;
import com.lvt.khvip.client.dto.ApprovalExistedER;
import com.lvt.khvip.client.dto.ResponseData;
import com.lvt.khvip.client.dto.ResponseListData;
import com.lvt.khvip.util.ConfProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
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
import java.util.List;

@Slf4j
public class ApprovalConfClient implements Serializable {

    private static String url = ConfProperties.getProperty("backend.enpoint");

    private ObjectMapper mapper;

    public ApprovalConfClient() {
        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<ApprovalConfDto> listApproval() {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            StringBuilder urlBuilder = new StringBuilder(this.url.concat("/approvals"));
            HttpGet http = new HttpGet(urlBuilder.toString());
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            ResponseListData<ApprovalConfDto> rs = mapper.readValue(jsonResponse,
                    new TypeReference<ResponseListData<ApprovalConfDto>>() {
                    });
            return rs.getData();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public ApprovalExistedER approvalExistedER(long autoid) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            StringBuilder urlBuilder = new StringBuilder(this.url.concat("/approval-existed?autoid=" + autoid));
            HttpGet http = new HttpGet(urlBuilder.toString());
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            ApprovalExistedER rs = mapper.readValue(jsonResponse,
                    new TypeReference<ApprovalExistedER>() {
                    });
            return rs;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public boolean isEditableApprovalExistedER(long autoid) {
        ApprovalExistedER rs = approvalExistedER(autoid);
        if (rs != null) {
            if (CollectionUtils.isEmpty(rs.getTimekeepingSheets()) && CollectionUtils.isEmpty(rs.getOtSheets())) {
                return true;
            }
        }
        return false;
    }

    public ResponseData<ApprovalConfDto> create(ApprovalConfDto data) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost http = new HttpPost(url.concat("/approval"));

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(data);
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
                ApprovalConfDto rs = mapper.readValue(jsonResponse, new TypeReference<ApprovalConfDto>() {
                });
                ResponseData<ApprovalConfDto> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<ApprovalConfDto> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<ApprovalConfDto>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseData<ApprovalConfDto> responseData = new ResponseData<>();
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }

    public ResponseData<ApprovalConfDto> update(ApprovalConfDto data) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut http = new HttpPut(url.concat("/approval"));

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(data);
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
                ApprovalConfDto rs = mapper.readValue(jsonResponse, new TypeReference<ApprovalConfDto>() {
                });
                ResponseData<ApprovalConfDto> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<ApprovalConfDto> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<ApprovalConfDto>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseData<ApprovalConfDto> responseData = new ResponseData<>();
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }

    public boolean delete(long autoId) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpDelete http = new HttpDelete(url.concat("/approval?autoid=" + autoId));
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
