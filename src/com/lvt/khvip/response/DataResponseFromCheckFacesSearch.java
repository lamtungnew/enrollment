package com.lvt.khvip.response;

import org.apache.commons.lang3.ArrayUtils;

public class DataResponseFromCheckFacesSearch {
	
	private double SCORE_IS_FACE_EXIST = 0.8;
	
	private String status;
	private DataSearch[] data; 

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public DataSearch[] getData() {
		return data;
	}

	public void setData(DataSearch[] data) {
		this.data = data;
	}
	
	public String getPeopleIdOfFace() {

		DataSearch firstData;
		if (ArrayUtils.isEmpty(data)) {
			return null;
		} else {
			firstData = data[0];
			if (firstData.getScore() >= SCORE_IS_FACE_EXIST) {
				return firstData.getPeople_id();
			} else {
				return null;
			}
		}
	}
	 
}
