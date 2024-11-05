package com.lvt.khvip.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;


@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class OtSheetDate implements Serializable{

    private int autoid;
    private int otId;
    private String otDate;
    private String startTime;
    private String toTime;
    private Date otDateTypeDate;
}
