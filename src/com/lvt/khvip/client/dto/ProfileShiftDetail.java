package com.lvt.khvip.client.dto;

import java.io.Serializable;

import com.lvt.khvip.client.dto.ResponseData.ResponseDataBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileShiftDetail implements Serializable {
	private Long autoid;
	private String shiftCode;
	private String shiftId;
	private String day;
	private String dayName;
	private String workingStartTime;
	private String workingEndTime;
	private String breakStartTime;
	private String breakEndTime;
	private String isOverTime;
	private String status;
}
