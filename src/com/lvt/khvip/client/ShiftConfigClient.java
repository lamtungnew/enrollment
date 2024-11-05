package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.dto.ResponseListData;
import com.lvt.khvip.client.dto.ShiftConfigDto;
import com.lvt.khvip.client.dto.ShiftConfigOrgDto;
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
import java.util.List;

@Slf4j
public class ShiftConfigClient implements Serializable {

    private static String url = ConfProperties.getProperty("backend.enpoint");

    private ObjectMapper mapper;

    public ShiftConfigClient() {
        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<ShiftConfigDto> listShiftConfig() {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet http = new HttpGet(url.concat("/shifts?status=1"));
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            ResponseListData<ShiftConfigDto> responseListData = mapper.readValue(jsonResponse,
                    new TypeReference<ResponseListData<ShiftConfigDto>>() {
                    });
            return responseListData.getData();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public List<ShiftConfigOrgDto> shiftConfigByGroup(Long orgId, Long groupId) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();

            StringBuilder urlBuilder = new StringBuilder(this.url.concat("/shift-orgs?"));
            urlBuilder.append("orgId=" + orgId);
            if (groupId != null) {
                urlBuilder.append("&groupId=" + groupId);
            }

            HttpGet http = new HttpGet(url.concat("/shift-orgs?orgId=" + orgId));
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            ResponseListData<ShiftConfigOrgDto> responseListData = mapper.readValue(jsonResponse,
                    new TypeReference<ResponseListData<ShiftConfigOrgDto>>() {
                    });
            return responseListData.getData();
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public ShiftConfigDto shiftConfigDetail(Integer id) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet http = new HttpGet(url.concat("/shift?autoid=" + id));
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            ShiftConfigDto shiftConfigDto = mapper.readValue(jsonResponse,
                    new TypeReference<ShiftConfigDto>() {
                    });
            return shiftConfigDto;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public ShiftConfigDto shiftConfigDetail(String code) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpGet http = new HttpGet(url.concat("/shift?autoid=-1&code=" + code));
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            ShiftConfigDto shiftConfigDto = mapper.readValue(jsonResponse,
                    new TypeReference<ShiftConfigDto>() {
                    });
            return shiftConfigDto;
        } catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public ShiftConfigDto create(ShiftConfigDto shift) {
        try {
            ShiftConfigDto insertData = ShiftConfigDto.builder()
                    .code(shift.getCode())
                    .name(shift.getName())
                    .status(shift.getStatus())
                    .workingDayType(shift.getWorkingDayType())
                    .shiftDetail(shift.getShiftDetail())
                    .oshiftDetail(shift.getOshiftDetail())
                    .shiftOrg(shift.getShiftOrg())
                    .build();

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost http = new HttpPost(url.concat("/shift"));


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

            ShiftConfigDto shiftConfigDto = mapper.readValue(jsonResponse, new TypeReference<ShiftConfigDto>() {
            });
            return shiftConfigDto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ShiftConfigDto update(ShiftConfigDto shift) {
        try {

            ShiftConfigDto updateData = ShiftConfigDto.builder()
                    .autoid(shift.getAutoid())
                    .code(shift.getCode())
                    .name(shift.getName())
                    .workingDayType(shift.getWorkingDayType())
                    .shiftDetail(shift.getShiftDetail())
                    .oshiftDetail(shift.getOshiftDetail())
                    .shiftOrg(shift.getShiftOrg())
                    .build();

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut http = new HttpPut(url.concat("/shift"));


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

            ShiftConfigDto shiftConfigDto = mapper.readValue(jsonResponse, new TypeReference<ShiftConfigDto>() {
            });
            return shiftConfigDto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(long autoId) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpDelete http = new HttpDelete(url.concat("/shift?autoid=" + autoId));
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
