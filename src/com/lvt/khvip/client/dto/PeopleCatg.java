package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeopleCatg implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;
	private String peopleId;
	private String fullName;
	private Date dateOfBirth;
	private String gender;
	private String customerType;
	private String lastCheckinTime;
	private String imagePath;
	private int groupId;
	private String mobilePhone;
	private String groupName;
	private String cusType;
	private int status;
	private String imagePathNoHost;
	
}
