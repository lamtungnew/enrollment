package com.lvt.khvip.client.author.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionListDto implements Serializable {
    private Map<String, List<ModuleRoleActionDto>> permissions;
    private List<AppModule> modules;
    private List<Roles> roles;
}
