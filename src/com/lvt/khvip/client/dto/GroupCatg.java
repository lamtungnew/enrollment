package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupCatg implements Serializable {
    private Integer groupId;
    private String groupCode;
    private String groupName;
    private String groupDescription;
    private Integer status;
    private Integer parentId;
    private Integer parentName;
    private Integer rootId;

}
