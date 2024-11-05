package com.lvt.khvip.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseListData<T> {
    private String object;
    private Integer totalCount;
    private Integer pageSize;
    private Integer pageIndex;
    private List<T> data;
	private String error;
	private String message;
}
