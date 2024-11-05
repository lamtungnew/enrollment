package com.lvt.khvip.client.author.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModuleRoleActionDto implements Serializable {
    private String moduleName;
    private long roleId;
    private long moduleActionId;
    private String moduleActionCode;
    private String moduleActionName;
    private boolean allow;
    private boolean oldAllow;
}
