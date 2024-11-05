package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.formula.functions.T;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportData<T> {
    private String importBy;
    private String attachment;
    private String title;
    private Integer totalrecords;
    private Integer failrecords;
    private List<T> objectImport;
}
