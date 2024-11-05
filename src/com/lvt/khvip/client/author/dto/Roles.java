package com.lvt.khvip.client.author.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.io.Serializable;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class Roles implements Serializable {
	private Long id;
	private String name;
	private Date createdTime;
	private Integer status;
	private Boolean approveable;
	private String description;
	private String error;
	private String message;
}
