package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lvt.khvip.client.dto.*;
import com.lvt.khvip.constant.Constants;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

@Slf4j
public class EmployeeClient implements Serializable {

    private static String url = ConfProperties.getProperty("backend.enpoint");

    private ObjectMapper mapper;

    public EmployeeClient() {
        mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public List<Employee> listPeople(Long depId, Long groupId, String fullName) {
        ResponseListData<Employee> rs = listPeople(depId, groupId, null, fullName, null, null, 0, 10000);
        if (rs != null && !CollectionUtils.isEmpty(rs.getData())) {
            return rs.getData();
        }
        return Collections.emptyList();
    }

    public ResponseListData<Employee> listPeople(String peopleId, String fullName, String mobilePhone, String imagePath,
                                                 Integer pageIndex, Integer pageSize) {
        return listPeople(null, null, peopleId, fullName, mobilePhone, imagePath, pageIndex, pageSize);
    }

    public ResponseListData<Employee> listPeople(Long depId, Long groupId, String peopleId, String fullName, String mobilePhone, String imagePath,
                                                 Integer pageIndex, Integer pageSize) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            StringBuilder urlBuilder = new StringBuilder(this.url.concat("/employees?"));
            urlBuilder.append("&page=" + pageIndex).append("&limit=" + pageSize);
            urlBuilder.append("&order_field=id&order_type=0");
            if (depId != null) {
                urlBuilder.append("&depId=" + depId);
            }
            if (groupId != null) {
                urlBuilder.append("&groupId=" + groupId);
            }
            if (peopleId != null) {
                urlBuilder.append("&peopleId=" + peopleId);
            }
            if (fullName != null) {
                urlBuilder.append("&fullName=" + URLEncoder.encode(fullName, StandardCharsets.UTF_8.toString()));
            }
            if (mobilePhone != null) {
                urlBuilder.append("&mobilePhone=" + mobilePhone);
            }
            if (imagePath != null) {
                urlBuilder.append("&imagePath=" + imagePath);
            }
            HttpGet http = new HttpGet(urlBuilder.toString());
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            ResponseListData<Employee> rs = mapper.readValue(jsonResponse,
                    new TypeReference<ResponseListData<Employee>>() {
                    });

            if (rs != null && rs.getData() != null) {
                rs.getData().forEach(item -> {
                    if (Constants.RoleConstants.GROUP_LEADER.equals(item.getRole())) {
                        item.setDepId(item.getGroupId());
                        item.setDepCode(item.getGroupCode());
                        item.setDepName(item.getGroupName());
                        item.setGroupId(null);
                        item.setGroupCode(null);
                        item.setGroupName(null);
                    }
                });
            }
            return rs;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public Employee employeeDetail(String peopleId) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            StringBuilder urlBuilder = new StringBuilder(this.url.concat("/employee?"));
            urlBuilder.append("people_id=" + peopleId);

            HttpGet http = new HttpGet(urlBuilder.toString());
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);
            Employee rs = mapper.readValue(jsonResponse,
                    new TypeReference<Employee>() {
                    });
            return rs;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    public ResponseData<Employee> create(Employee employee) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost http = new HttpPost(url.concat("/employee"));

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(employee);
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
                Employee rs = mapper.readValue(jsonResponse, new TypeReference<Employee>() {
                });
                ResponseData<Employee> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<Employee> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<Employee>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseData<Employee> responseData = new ResponseData<>();
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }

    public ResponseData<Employee> update(Employee employee) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut http = new HttpPut(url.concat("/employee"));

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(employee);
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
                Employee rs = mapper.readValue(jsonResponse, new TypeReference<Employee>() {
                });
                ResponseData<Employee> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<Employee> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<Employee>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseData<Employee> responseData = new ResponseData<>();
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }

    public ResponseData<Employee> updateImage(Employee employee) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut http = new HttpPut(url.concat("/employee/update/image"));

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(employee);
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
                Employee rs = mapper.readValue(jsonResponse, new TypeReference<Employee>() {
                });
                ResponseData<Employee> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<Employee> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<Employee>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseData<Employee> responseData = new ResponseData<>();
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }

    public ResponseData<Boolean> delete(String peopleId) {
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpDelete http = new HttpDelete(url.concat("/employee?peopleId=" + peopleId));
            http.setHeader("Content-type", "application/json");
            HttpResponse response = client.execute(http);
            HttpEntity entityResponse = response.getEntity();
            String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
            if (response.getStatusLine().getStatusCode() == 200) {
                Boolean rs = mapper.readValue(jsonResponse, new TypeReference<Boolean>() {
                });
                ResponseData<Boolean> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<Boolean> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<Boolean>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ResponseData<Boolean> responseData = new ResponseData<>();
            responseData.setError("error");
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }

    public ImportData<Employee> createBatch(String userName, String importTitle, String string, List<Employee> data) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost http = new HttpPost(url.concat("/employee/batch"));

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
                ImportData<Employee> responseData = mapper.readValue(jsonResponse, new TypeReference<ImportData<Employee>>() {
                });
                return responseData;
            } else {
                ImportData<Employee> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ImportData<Employee>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ImportData<Employee> responseData = new ImportData<>();
            return responseData;
        }
    }

    public ResponseData<Boolean> changePass(ChangePassworDto changePassworDto) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut http = new HttpPut(url.concat("/employee/change-password"));

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(changePassworDto);
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
                Boolean rs = mapper.readValue(jsonResponse, new TypeReference<Boolean>() {
                });
                ResponseData<Boolean> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<Boolean> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<Boolean>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseData<Boolean> responseData = new ResponseData<>();
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }

    public ResponseData<Boolean> resetPass(ChangePassworDto changePassworDto) {
        try {

            CloseableHttpClient client = HttpClients.createDefault();
            HttpPut http = new HttpPut(url.concat("/employee/reset-password"));

            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(changePassworDto);
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
                Boolean rs = mapper.readValue(jsonResponse, new TypeReference<Boolean>() {
                });
                ResponseData<Boolean> responseData = new ResponseData<>();
                responseData.setData(rs);
                return responseData;
            } else {
                ResponseData<Boolean> responseData = mapper.readValue(jsonResponse,
                        new TypeReference<ResponseData<Boolean>>() {
                        });
                return responseData;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            ResponseData<Boolean> responseData = new ResponseData<>();
            responseData.setMessage(e.getMessage());
            return responseData;
        }
    }
}
