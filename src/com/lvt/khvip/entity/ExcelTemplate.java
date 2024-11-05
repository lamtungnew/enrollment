package com.lvt.khvip.entity;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode()
@Data
public class ExcelTemplate implements Serializable
{
    private int excelTemplateId;
    private String code;
    private String type;
    private String title;
    private String fileName;
    private Integer sheetSeqNo;
    private Integer startRow;
    private Integer activeInd;
    private String createdBy;
    private Timestamp createdDate;
    private String modifiedBy;
    private Timestamp modifiedDate;
    private String inactBy;
    private Timestamp inactDate;

    private List<ExcelTemplateDet> listTempDetail = new ArrayList<ExcelTemplateDet>();

	public int getExcelTemplateId() {
		return excelTemplateId;
	}

	public void setExcelTemplateId(int excelTemplateId) {
		this.excelTemplateId = excelTemplateId;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Integer getSheetSeqNo() {
		return sheetSeqNo;
	}

	public void setSheetSeqNo(Integer sheetSeqNo) {
		this.sheetSeqNo = sheetSeqNo;
	}

	public Integer getStartRow() {
		return startRow;
	}

	public void setStartRow(Integer startRow) {
		this.startRow = startRow;
	}

	public Integer getActiveInd() {
		return activeInd;
	}

	public void setActiveInd(Integer activeInd) {
		this.activeInd = activeInd;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public Timestamp getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Timestamp createdDate) {
		this.createdDate = createdDate;
	}

	public String getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	public Timestamp getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Timestamp modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public String getInactBy() {
		return inactBy;
	}

	public void setInactBy(String inactBy) {
		this.inactBy = inactBy;
	}

	public Timestamp getInactDate() {
		return inactDate;
	}

	public void setInactDate(Timestamp inactDate) {
		this.inactDate = inactDate;
	}

	public List<ExcelTemplateDet> getListTempDetail() {
		return listTempDetail;
	}

	public void setListTempDetail(List<ExcelTemplateDet> listTempDetail) {
		this.listTempDetail = listTempDetail;
	}        
}
