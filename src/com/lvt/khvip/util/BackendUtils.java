package com.lvt.khvip.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class BackendUtils {

	private static String endpoint = ConfProperties.getProperty("backend.enpoint");

	public static String sendPost(String api, Object body, String methodType) {
		StringBuffer response = new StringBuffer();
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(body);

			String urlStr = endpoint + api;
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestMethod(methodType);
			conn.setRequestProperty("Content-Type", "application/json");

			String strJsonBody = new ObjectMapper().writeValueAsString(body);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(strJsonBody);
			wr.flush();

			System.out.println("error: "+conn.getResponseMessage());

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				response.append(output);
			}
			wr.close();
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
	}

	public static String sendGET(String api, String params) {
		StringBuffer response = new StringBuffer();
		try {
			//params = "?1&fullName=Do Minh Hieu&page=1&limit=10";
			String urlStr =  endpoint + api + "?1"+params;
			URL obj = new URL(urlStr);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
	        con.setRequestProperty("content-type", "application/json;  charset=utf-8");
			//con.setRequestProperty("Content-Type", "application/json");
			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				// success
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));

				String inputLine;

				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();

			} else {
				System.out.println("GET request not worked");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response.toString();
	}

	public static <T> Object convertJsonToObj(String response, Class<T> cls) {
//		CustomizedObjectTypeAdapter adapter = new CustomizedObjectTypeAdapter();
//		Gson gson = new GsonBuilder()
//		        .registerTypeAdapter(Map.class, adapter)
//		        .registerTypeAdapter(List.class, adapter)
//		        .create();
//		JsonParser parser = new JsonParser();
//		JsonObject object = (JsonObject) parser.parse(response);
//		T rs = gson.fromJson(object, cls); 
		// .fromJson(draft, new TypeToken<ArrayList<Response>>(){}.getType());
		ObjectMapper mapper = new ObjectMapper();
		try {
			T rs = mapper.readValue(response, cls);
			return rs;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String saveTimeKeepingSheet(String api, Object body, String methodType) {
		StringBuffer response = new StringBuffer();
		try {
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

			String urlStr = endpoint + api;
			URL url = new URL(urlStr);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setUseCaches(false);
			conn.setDoInput(true);
			conn.setDoOutput(true);

			conn.setRequestMethod(methodType);
			conn.setRequestProperty("Content-Type", "application/json");

			String strJsonBody = new ObjectMapper().writeValueAsString(body);

			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write(strJsonBody);
			wr.flush();

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			while ((output = br.readLine()) != null) {
				response.append(output);
			}

			wr.close();
			br.close();
		} catch (Exception e) {
			return "400";
		}
		return "200";
	}

	public static <T> List<T> stringToArray(String s, Class<T[]> clazz) {
//		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
//
//		T[] arr = gson.fromJson(s, clazz);
//		return Arrays.asList(arr); // or return Arrays.asList(new Gson().fromJson(s, clazz)); for a one-liner
	
		ObjectMapper mapper = new ObjectMapper();
		try {
			T[] arr = mapper.readValue(s, clazz);
			return Arrays.asList(arr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
