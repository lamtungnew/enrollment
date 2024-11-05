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
public class ChangePassworDto implements Serializable {
    public String userName;
    public String password;
    public String newPassword;
    public String newPasswordCf;
}
