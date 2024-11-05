package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.lvt.khvip.util.StringUtils;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseData<T> {
	private String error;
	private String message;
	private T data;

	public boolean isError() {
		if (!StringUtils.isEmpty(error) && !StringUtils.isEmpty(message)) {
			return true;
		}
		return false;
	}
}
