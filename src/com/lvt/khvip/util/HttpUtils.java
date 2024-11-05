package com.lvt.khvip.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.primefaces.model.file.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.constant.StatusConstant;
import com.lvt.khvip.response.DataResponseFromCheckFacesSearch;
import com.lvt.khvip.response.DataResponseFromRegisterFaceSearch;
import com.lvt.khvip.response.DataResponseFromRemoveFacesSearch;

public class HttpUtils implements Serializable {
	private static final long serialVersionUID = 6837711851266019238L;
	private static final Logger log = LoggerFactory.getLogger(HttpUtils.class);
	private static final String URL_FACE_REG = ConfProperties.getProperty("url.reg.face");

	private static final String URL_FACE_DEL = ConfProperties.getProperty("url.del.face"); 
	private static final String URL_FACE_SEARCH = ConfProperties.getProperty("url.search.face");

	private static HttpClient httpClient;
	private static HttpPost httpPost;

	public DataResponseFromRegisterFaceSearch callAPIToCreate(String capturedImagePath, String peopleId) {

		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
			String uri = capturedImagePath;
			log.info("Face image URL: {}", uri);
			HttpGet request = new HttpGet(uri);
			HttpResponse response = httpClient.execute(request);
			InputStream content = response.getEntity().getContent();
			String base64Image = Base64.getEncoder().encodeToString(IOUtils.toByteArray(content));

			CloseableHttpClient client = HttpClients.createDefault();
			log.info("Face reg URL: {}", URL_FACE_REG);
			HttpPost httpPost = new HttpPost(URL_FACE_REG);

			long millis = System.currentTimeMillis();
			long timestampSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);

			String json = "{\"image\":\" " + base64Image + "\",\"people_id\":\"" + peopleId
					+ "\",\"created_at\":\"" + timestampSeconds + "\",\"source\":\"" + Constants.SOURCE + "\",\"is_live_check\":" + "false" + "}";
//			String json = "{\"image\":\" " + base64Image + "\",\"people_id\":\"" + peopleId
//					+ "\",\"created_at\":\"" + timestampSeconds + "\" }";

			StringEntity entity = new StringEntity(json);
			entity.setContentType("application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setEntity(entity);

			HttpResponse responseRegister = client.execute(httpPost);
			HttpEntity entityRegister = responseRegister.getEntity();

			// use org.apache.http.util.EntityUtils to read json as string
			String jsonResponse = EntityUtils.toString(entityRegister, StandardCharsets.UTF_8);
			log.info("Reg face response {}", jsonResponse);
			
			Gson gson = new Gson();
			DataResponseFromRegisterFaceSearch dataResponse = gson.fromJson(jsonResponse, DataResponseFromRegisterFaceSearch.class);
			return dataResponse;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public DataResponseFromRegisterFaceSearch callAPIToCreateHaveFile(UploadedFile imagePath, String peopleId) {
		try {
			InputStream inputStream = imagePath.getInputStream();
			String base64Image = Base64.getEncoder().encodeToString(IOUtils.toByteArray(inputStream));

			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(URL_FACE_REG);

			Date date = new Date();
			long millis = date.getTime();
			long timestampSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);

			String json = "{\"image\":\" " + base64Image + "\",\"people_id\":\"" + peopleId
					+ "\",\"created_at\":\"" + timestampSeconds + "\",\"source\":\"" + Constants.SOURCE + "\",\"is_live_check\":" + "false" + "}";
//			String json = "{\"image\":\" " + base64Image + "\",\"people_id\":\"" + peopleId
//					+ "\",\"created_at\":\"" + timestampSeconds + "\" }";

			StringEntity entity = new StringEntity(json);
			entity.setContentType("application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setEntity(entity);

			HttpResponse response1 = client.execute(httpPost);

			HttpEntity entity1 = response1.getEntity();

			// use org.apache.http.util.EntityUtils to read json as string
			String jsonResponse = EntityUtils.toString(entity1, StandardCharsets.UTF_8);
			log.info("Reg face response {}", jsonResponse);

			Gson gson = new Gson();
			DataResponseFromRegisterFaceSearch dataResponse = gson.fromJson(jsonResponse, DataResponseFromRegisterFaceSearch.class);
			
			return dataResponse;
		} catch (Exception exception) {
			exception.printStackTrace();
			return null;
		}
	}

	public DataResponseFromRemoveFacesSearch callAPIToRemove(String peopleId) {

		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost httpPostRemove = new HttpPost(URL_FACE_DEL);

			String json = "{ \"people_id\":\"" + peopleId + "\",\"source\":\"" + Constants.SOURCE + "\"}";
//			String json = "{ \"people_id\":\"" + peopleId + "\"}";

			StringEntity entityRemove = new StringEntity(json);
			entityRemove.setContentType("application/json");
			httpPostRemove.setHeader("Content-type", "application/json");
			httpPostRemove.setEntity(entityRemove);

			HttpResponse responseRemove = client.execute(httpPostRemove);

			HttpEntity entityRemoveResponse = responseRemove.getEntity();

			String jsonResponse = EntityUtils.toString(entityRemoveResponse, StandardCharsets.UTF_8);
			log.info("Remove face response {}", jsonResponse);

			Gson gsonRemove = new Gson();
			DataResponseFromRemoveFacesSearch dataRemoveResponse = gsonRemove.fromJson(jsonResponse, DataResponseFromRemoveFacesSearch.class);
			return dataRemoveResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean checkLiveless(String base64Image) {
		boolean retValue = false;
		httpClient = HttpClients.createDefault();
		httpPost = new HttpPost(Constants.URL_FACE_CHECK_LIVELESS);

		String json = "{\"image\": \"" + base64Image + "\"}";
		
		StringEntity jsonParameter = new StringEntity(json, ContentType.APPLICATION_JSON);
		httpPost.setHeader("Content-Type", "application/json");
		httpPost.setEntity(jsonParameter);

		try {
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity respEntity = response.getEntity();

			if (respEntity != null) {
				// EntityUtils to get the response content
				String content = EntityUtils.toString(respEntity);
				Gson gson = new Gson();
				JsonElement jelem = gson.fromJson(content, JsonElement.class);
				JsonObject jobj = jelem.getAsJsonObject();
				int code = jobj.get("Code").getAsInt();
				String message = jobj.get("Message").getAsString();

				if (StatusConstant.CODE_CHECK_LIVELESS_SUCCESS == code) {
					retValue = true;
					log.info("check live is ok: " + message);
				} else {
					log.info("check live is not ok: " + message);
				}
			}
		} catch (ClientProtocolException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally {
			httpPost.releaseConnection();
		}
		return retValue;
	}

	public DataResponseFromRegisterFaceSearch callAPIToRegister(String base64Image, String peopleId) {
		try {
			httpClient = HttpClients.createDefault();
			httpPost = new HttpPost(URL_FACE_REG);

			Date date = new Date();
			long millis = date.getTime();
			long timestampSeconds = TimeUnit.MILLISECONDS.toSeconds(millis);

			String json = "{\"image\":\" " + base64Image + "\",\"people_id\":\"" + peopleId
					+ "\",\"created_at\":\"" + timestampSeconds + "\",\"source\":\"" + Constants.SOURCE + "\",\"is_live_check\":" + "false" + "}";
//			String json = "{\"image\":\" " + base64Image + "\",\"people_id\":\"" + peopleId
//					+ "\",\"created_at\":\"" + timestampSeconds + "\" }";

			StringEntity entity = new StringEntity(json);
			entity.setContentType("application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setEntity(entity);

			HttpResponse responseRegister = httpClient.execute(httpPost);
			HttpEntity entityRegister = responseRegister.getEntity();
			String jsonResponse = EntityUtils.toString(entityRegister, StandardCharsets.UTF_8);
			log.info("Reg face response {}", jsonResponse);

			Gson gson = new Gson();
			DataResponseFromRegisterFaceSearch dataResponse = gson.fromJson(jsonResponse, DataResponseFromRegisterFaceSearch.class);
			
			return dataResponse;
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			return null;
		} finally {
			httpPost.releaseConnection();
		}
	}
	
	public DataResponseFromCheckFacesSearch callAPIToSearch(String source, String base64Image) {

		try {
			CloseableHttpClient client = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(URL_FACE_SEARCH);

			String json = "{ \"source\":\"" + source + "\",\"image\":\"" + base64Image + "\"}";

			StringEntity entity = new StringEntity(json);
			entity.setContentType("application/json");
			httpPost.setHeader("Content-type", "application/json");
			httpPost.setEntity(entity);

			HttpResponse response = client.execute(httpPost);

			HttpEntity entityResponse = response.getEntity();

			String jsonResponse = EntityUtils.toString(entityResponse, StandardCharsets.UTF_8);
			log.info("callAPIToSearch {}", jsonResponse);

			Gson gsonRemove = new Gson();
			DataResponseFromCheckFacesSearch dataResponse = gsonRemove.fromJson(jsonResponse, DataResponseFromCheckFacesSearch.class);
			return dataResponse;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
