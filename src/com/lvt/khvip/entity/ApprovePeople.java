package com.lvt.khvip.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ApprovePeople implements Serializable {
    private List<People> peopleIds;
    private String role;
    private Integer ruleId;
}
