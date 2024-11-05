package com.lvt.khvip.client.author.dto;

import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppModuleAction implements Serializable {

	private long id;
	private long moduleId;
	private String code;
	private String name;
	private int status;
}
