package com.lvt.khvip.client.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApParamDto implements Serializable {
	public Integer autoid;
	public String group;
	public String name;
	public String value;
	public String description;
	public String error;
	public String message;
}
