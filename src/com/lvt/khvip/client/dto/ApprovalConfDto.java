package com.lvt.khvip.client.dto;

import java.io.Serializable;
import java.util.Date;

import com.lvt.khvip.client.dto.GroupCatg.GroupCatgBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalConfDto implements Serializable {
	public Integer autoid;
	public Integer groupId;
	public Integer depId;
	public String approvalType;
	public Integer approvalLevel;
	public String approvalLevel1;
	public String approvalLevel2;
	public String createdBy;
	public Date createdAt;
	public Date modifiedAt;
}
