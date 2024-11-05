package com.lvt.khvip.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.entity.*;
import com.lvt.khvip.util.BackendUtils;
import com.lvt.khvip.util.StringUtils;

import java.io.Serializable;

public class ApprovePeopleClient implements Serializable {

	private ObjectMapper mapper;

	public ApprovePeopleClient(){
		mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
	}

	public ApprovePeople getApprovePeople(String peopleId, String username, String approvalType, Integer approvalLevel){
		try {
			StringBuilder param = new StringBuilder("&1=1");

			if (!StringUtils.isEmpty(peopleId)) {
				param.append("&peopleId=" + peopleId);
			}

			if (!StringUtils.isEmpty(username)) {
				param.append("&username=" + username);
			}

			if (!StringUtils.isEmpty(approvalType)) {
				param.append("&approvalType=" + approvalType);
			}

			if (!StringUtils.isEmpty(approvalLevel)) {
				param.append("&approvalLevel=" + approvalLevel);
			}

			String response = BackendUtils.sendGET(Constants.BackendApis.APPROVAL_PEOPLE, param.toString());

			ApprovePeople approvePeople = mapper.readValue(response,
					new TypeReference<ApprovePeople>() {
					});

			return approvePeople;
		}catch (Exception e){
			e.printStackTrace();
		}
		return null;
	}
}
