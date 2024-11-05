package com.lvt.khvip.client.author.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionDto implements Serializable {
	private long rownum;

	private long roleId;
	private String roleCode;

	private long moduleId;
	private String moduleCode;
	private String moduleUrl;

	private long moduleActionId;
	private String moduleActionCode;

	private boolean allow;
}
