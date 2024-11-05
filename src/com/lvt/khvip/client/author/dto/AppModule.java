package com.lvt.khvip.client.author.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppModule implements Serializable {
    private long id;
    private String code;
    private String name;
    private int status;
    private List<AppModuleAction> actions;
}
