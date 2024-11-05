package com.lvt.khvip.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class TimekeepingSheet implements Serializable{

    private int autoid;
    private String peopleId;
    private String description;
    private Date createdAt;
    private String createdBy;
    private String timeKeepingImage;
    private String state;
    private String approvedBy;
    private Date approvedAt;
    private String dateType;
    private String peopleIds;
    private String keepingDates;
    private String keepingDate;
    private String peopleIdAndKeepingDate;

    private Date keepingDateCalendar;

    private People people = new People();
}
