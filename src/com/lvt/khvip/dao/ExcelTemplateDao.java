/**
 *
 */
package com.lvt.khvip.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.lvt.khvip.entity.ExcelTemplate;
import com.lvt.khvip.entity.ExcelTemplateDet;
import com.lvt.khvip.util.Utility;

/**
 * @author: tuha.lvt
 *
 * @date:
 */

@ManagedBean
@SessionScoped
public class ExcelTemplateDao {
	private static final Logger log = LoggerFactory.getLogger(ExcelTemplateDao.class);

    public static List<ExcelTemplate> getExcelTemplate(String code) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ExcelTemplate excelTemplate = null;
        List<ExcelTemplateDet> listExcelTemplateDet = null;
        List<ExcelTemplate> result = new ArrayList();
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String createdAtStr = "";
            
            sqlBuilder.append(" select EXCEL_TEMPLATE_ID, CODE, TYPE, TITLE, FILE_NAME, SHEET_SEQ_NO, START_ROW, "
            		+ "ACTIVE_IND, CREATED_BY, CREATED_DATE, MODIFIED_BY, MODIFIED_DATE, INACT_BY, INACT_DATE "
            		+ "from excel_template ");
            sqlBuilder.append("where CODE = '"+code+"' and ACTIVE_IND = 1 ");
                       
            
            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBuilder.toString());
            while (resultSet.next()) {
            	excelTemplate = new ExcelTemplate();
            	excelTemplate.setExcelTemplateId(resultSet.getString("EXCEL_TEMPLATE_ID") != null ? Integer.parseInt(resultSet.getString("EXCEL_TEMPLATE_ID")) : null);
            	excelTemplate.setCode(resultSet.getString("CODE") != null ? resultSet.getString("CODE") : "");
            	excelTemplate.setType(resultSet.getString("TYPE") != null ? resultSet.getString("TYPE") : "");
            	excelTemplate.setTitle(resultSet.getString("TITLE") != null ? resultSet.getString("TITLE") : "");
            	excelTemplate.setFileName(resultSet.getString("FILE_NAME"));
            	excelTemplate.setSheetSeqNo(resultSet.getString("SHEET_SEQ_NO") != null ? Integer.parseInt(resultSet.getString("SHEET_SEQ_NO")) : null);
            	excelTemplate.setStartRow(resultSet.getString("START_ROW") != null ? Integer.parseInt(resultSet.getString("START_ROW")) : null);
            	excelTemplate.setActiveInd(resultSet.getString("ACTIVE_IND") != null ? Integer.parseInt(resultSet.getString("ACTIVE_IND")) : null);

            	            
            	listExcelTemplateDet = getExcelTemplateDet(excelTemplate.getExcelTemplateId(), 1);
            	excelTemplate.setListTempDetail(listExcelTemplateDet);
            	
            	result.add(excelTemplate);
            }
            String json = new Gson().toJson(result);
            log.info("Total Records Fetched: " + json);
            
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            Utility.closeObject(resultSet);
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }
    
    public static List<ExcelTemplateDet> getExcelTemplateDet(int uploadTemplateId, int activeInd) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ExcelTemplateDet excelTemplateDet = null;
        List<ExcelTemplateDet> result = new ArrayList();
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String createdAtStr = "";
            
            sqlBuilder.append("select EXCEL_TEMPLATE_DET_ID, EXCEL_TEMPLATE_ID, FIELD_NAME, FIELD_TYPE, POSITION, WIDTH, ALIGNMENT, COLUMN_NAME, "
            		+ "COLUMN_FULL_NAME, PATTERN, MANDATORY_IND, MAX_LENGTH, DEFAULT_VALUE, "
            		+ "COLUMN_SIZE, ACTIVE_IND, CELL_TYPE "
            		+ "from excel_template_det ");
            sqlBuilder.append("where EXCEL_TEMPLATE_ID = "+uploadTemplateId+" and ACTIVE_IND = '"+activeInd+"' ");
                                   
            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBuilder.toString());
            while (resultSet.next()) {
            	excelTemplateDet = new ExcelTemplateDet();
            	excelTemplateDet.setExcelTemplateDetId(resultSet.getString("EXCEL_TEMPLATE_DET_ID") != null ? Integer.parseInt(resultSet.getString("EXCEL_TEMPLATE_DET_ID")) : null);
            	excelTemplateDet.setExcelTemplateId(resultSet.getString("EXCEL_TEMPLATE_ID") != null? Integer.parseInt(resultSet.getString("EXCEL_TEMPLATE_ID")) : null);
            	excelTemplateDet.setFieldName(resultSet.getString("FIELD_NAME") != null ? resultSet.getString("FIELD_NAME") : "");
            	excelTemplateDet.setFieldType(resultSet.getString("FIELD_TYPE") != null ? resultSet.getString("FIELD_TYPE") : "");
            	excelTemplateDet.setPosition(resultSet.getString("POSITION") != null ? Integer.parseInt(resultSet.getString("POSITION")) : null);
            	excelTemplateDet.setWidth(resultSet.getString("WIDTH") != null ? Integer.parseInt(resultSet.getString("WIDTH")) : null);
            	excelTemplateDet.setAlignment(resultSet.getString("ALIGNMENT") != null ? resultSet.getString("ALIGNMENT") :"");
            	excelTemplateDet.setColumnName(resultSet.getString("COLUMN_NAME") != null ? resultSet.getString("COLUMN_NAME") :"");
            	excelTemplateDet.setColumnFullName(resultSet.getString("COLUMN_FULL_NAME") != null ? resultSet.getString("COLUMN_FULL_NAME") :"");
            	excelTemplateDet.setPattern(resultSet.getString("PATTERN") != null ? resultSet.getString("PATTERN") :"");
            	excelTemplateDet.setMandatoryInd(resultSet.getString("MANDATORY_IND") != null ? Integer.parseInt(resultSet.getString("MANDATORY_IND")) : null);
            	excelTemplateDet.setMaxLength(resultSet.getString("MAX_LENGTH") != null? Integer.parseInt(resultSet.getString("MAX_LENGTH")) : null);
            	excelTemplateDet.setDefaultValue(resultSet.getString("DEFAULT_VALUE") != null ? resultSet.getString("DEFAULT_VALUE") :"");
            	excelTemplateDet.setColumnSize(resultSet.getString("COLUMN_SIZE")  != null ? Integer.parseInt(resultSet.getString("COLUMN_SIZE")) : null);
            	excelTemplateDet.setActiveInd(resultSet.getString("ACTIVE_IND") != null ? Integer.parseInt(resultSet.getString("ACTIVE_IND")) : null);
            	excelTemplateDet.setCellType(resultSet.getString("CELL_TYPE") != null ? resultSet.getString("CELL_TYPE") :"");

            	
            	result.add(excelTemplateDet);
            }
            String json = new Gson().toJson(result);
            log.info("Total Records Fetched: " + json);
            
            return result;
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
            return new ArrayList<>();
        } finally {
            Utility.closeObject(resultSet);
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }
}
