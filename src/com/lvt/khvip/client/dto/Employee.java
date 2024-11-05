package com.lvt.khvip.client.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {
	public Integer id;
    public String peopleId;
    public String fullName;    
    public String userName;
    public String role;
    public String roleDescription;
    public String password;
    public Date dateOfBirth;
    public String gender;
    public String customerType;
    public String lastCheckinTime;
    public String imagePath;
    public Integer groupId;
    public String mobilePhone;
    public String email;
    public String groupName;
    public String groupCode;
    public String cusType;
    public Integer status;
    public String imagePathNoHost;
    public Integer depId;
    public String depCode;
    public String depName;
    public String error;
    public String message;
}
