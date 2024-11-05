package com.lvt.khvip.entity;

import java.util.List;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportExcelDto<T> {
	private List<T> objectImport;
	private int totalrecords;
	private int failrecords;
	private String importBy;
	private String attachment;
	private String title;
}
