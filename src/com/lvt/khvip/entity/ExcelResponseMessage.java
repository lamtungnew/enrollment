package com.lvt.khvip.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExcelResponseMessage {
    private String message;
    private String code;
    private String type;
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
    
    
}

