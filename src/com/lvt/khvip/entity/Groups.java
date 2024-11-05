package com.lvt.khvip.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@Getter
@Setter
@SessionScoped
@ManagedBean
public class Groups implements Serializable {
	private static final long serialVersionUID = 7219223607972970605L;

	private int groupId;
	private String groupCode;
	private String groupName;
	private String manager;
	private String groupDescription;
	private Integer status;
	private Integer parentId;
	private Integer rootId;
}
