package com.lvt.khvip.client.dto;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileShift implements Serializable {
	private Long autoid;
	private String code;
	private String name;
	private String startTimeFrom;
	private String endTimeFrom;
	private String description;
	private String startTimeTo;
	private String endTimeTo;
	private String workingDayType;

	private List<ProfileShiftDetail> shiftDetail;
}
