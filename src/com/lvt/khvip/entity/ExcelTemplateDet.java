package com.lvt.khvip.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@EqualsAndHashCode()
@Setter
@Getter
public class ExcelTemplateDet implements Serializable
{
    private int excelTemplateDetId;
    private Integer excelTemplateId;
    private String fieldName;
    private String fieldType;
    private Integer position;
    private String columnName;
    private String columnFullName;
    private String pattern;
    private Integer mandatoryInd;
    private Integer maxLength;
    private String defaultValue;
    private Integer needCapitalized;
    private Integer columnSize;
    private Integer activeInd;
    private Integer width;
    private String alignment;
    private Integer noneDuplicated;
    private String cellType;
    
	public int getExcelTemplateDetId() {
		return excelTemplateDetId;
	}
	public void setExcelTemplateDetId(int excelTemplateDetId) {
		this.excelTemplateDetId = excelTemplateDetId;
	}
	public Integer getExcelTemplateId() {
		return excelTemplateId;
	}
	public void setExcelTemplateId(Integer excelTemplateId) {
		this.excelTemplateId = excelTemplateId;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getFieldType() {
		return fieldType;
	}
	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}
	public Integer getPosition() {
		return position;
	}
	public void setPosition(Integer position) {
		this.position = position;
	}
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getColumnFullName() {
		return columnFullName;
	}
	public void setColumnFullName(String columnFullName) {
		this.columnFullName = columnFullName;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public Integer getMandatoryInd() {
		return mandatoryInd;
	}
	public void setMandatoryInd(Integer mandatoryInd) {
		this.mandatoryInd = mandatoryInd;
	}
	public Integer getMaxLength() {
		return maxLength;
	}
	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public Integer getNeedCapitalized() {
		return needCapitalized;
	}
	public void setNeedCapitalized(Integer needCapitalized) {
		this.needCapitalized = needCapitalized;
	}
	public Integer getColumnSize() {
		return columnSize;
	}
	public void setColumnSize(Integer columnSize) {
		this.columnSize = columnSize;
	}
	public Integer getActiveInd() {
		return activeInd;
	}
	public void setActiveInd(Integer activeInd) {
		this.activeInd = activeInd;
	}
	public Integer getWidth() {
		return width;
	}
	public void setWidth(Integer width) {
		this.width = width;
	}
	public String getAlignment() {
		return alignment;
	}
	public void setAlignment(String alignment) {
		this.alignment = alignment;
	}
	public Integer getNoneDuplicated() {
		return noneDuplicated;
	}
	public void setNoneDuplicated(Integer noneDuplicated) {
		this.noneDuplicated = noneDuplicated;
	}
	public String getCellType() {
		return cellType;
	}
	public void setCellType(String cellType) {
		this.cellType = cellType;
	}       
}
