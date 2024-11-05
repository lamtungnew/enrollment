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

import com.lvt.khvip.entity.*;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utility;

/**
 * @author: tuha.lvt
 *
 * @date:
 */

@ManagedBean
@SessionScoped
public class TimekeepingExplanationDao {
	private static final Logger log = LoggerFactory.getLogger(TimekeepingExplanationDao.class);

    public static List<TimekeepingSheetExcel> getListTimeKeepingSheet(TimekeepingSheet condition) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        TimekeepingSheetExcel timekeepingSheet = null;
        List<TimekeepingSheetExcel> result = new ArrayList();
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String createdAtStr = "";
            
            sqlBuilder.append(" select ts.autoid, p.people_id, p.full_name, p.mobile_phone, p.Gender, p.date_of_birth, ts.created_at, ts.description, ts.state, g.group_name, ct.name customer_type, ts.keeping_date ");
            sqlBuilder.append("from timekeeping_sheet ts inner join people p on ts.people_id = p.people_id ");
            sqlBuilder.append("inner join customertype ct on ct.id = p.customer_type ");
            sqlBuilder.append("inner join groups g on p.group_id = g.group_id where 1=1 ");
            
            if (condition != null) {
            	if (condition.getPeople() !=null && !StringUtils.isEmpty(condition.getPeople().getFullName())) {
                	sqlBuilder.append("and p.full_name like '%"+condition.getPeople().getFullName()+"%' ");
                }
                
                if (condition.getPeople() !=null && !StringUtils.isEmpty(condition.getPeople().getPeopleId())) {
                	sqlBuilder.append("and p.people_id like '%"+condition.getPeople().getPeopleId()+"%' ");
                }
                
                if (condition.getPeople() !=null && !StringUtils.isEmpty(condition.getPeople().getGroupName())) {
                	sqlBuilder.append("and g.group_name like '%"+condition.getPeople().getGroupName()+"%' ");
                }
                
                if (condition.getCreatedAt() != null) {
                	createdAtStr = simpleDateFormat.format(condition.getCreatedAt());
                	sqlBuilder.append("and DATE_FORMAT(ts.created_at, '%d/%m/%Y') = '"+createdAtStr+"' ");
                }
                
                if (!StringUtils.isEmpty(condition.getState())) {
                	sqlBuilder.append("and ts.state = '"+condition.getState()+"' ");
                }

                if (!StringUtils.isEmpty(condition.getPeopleIdAndKeepingDate())) {
                    sqlBuilder.append("and concat(ts.people_id,'||',ts.keeping_date) in ("+condition.getPeopleIdAndKeepingDate()+") ");
                }

            }
                        
            sqlBuilder.append("order by ts.created_at desc");
            
            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBuilder.toString());
            while (resultSet.next()) {
            	timekeepingSheet = new TimekeepingSheetExcel();
            	timekeepingSheet.setPeopleId(resultSet.getString("people_id"));
            	timekeepingSheet.setFullName(resultSet.getString("full_name"));
            	timekeepingSheet.setMobilePhone(resultSet.getString("mobile_phone"));
            	timekeepingSheet.setGender(resultSet.getString("Gender"));
            	timekeepingSheet.setDateOfBirth(resultSet.getString("date_of_birth"));
            	timekeepingSheet.setGroupName(resultSet.getString("group_name"));
            	timekeepingSheet.setCustomerType(resultSet.getString("customer_type"));
            	timekeepingSheet.setDateOfBirth2(resultSet.getDate("date_of_birth"));
            	timekeepingSheet.setDescription(resultSet.getString("description"));
                timekeepingSheet.setKeepingDateString(resultSet.getString("keeping_date"));

                if (resultSet.getString("state").equals("1")) {
                	timekeepingSheet.setState(Constants.TimeKeepingSheetConstants.APPROVED_STATUS);

            	} else if (resultSet.getString("state").equals("2")) {
                	timekeepingSheet.setState(Constants.TimeKeepingSheetConstants.DENY_STATUS);
            		
            	} else {
                	timekeepingSheet.setState(Constants.TimeKeepingSheetConstants.NEW_STATUS);
            		
            	}
            	            	
            	timekeepingSheet.setCreatedAt(resultSet.getDate("created_at"));    	
            	timekeepingSheet.setAutoid(resultSet.getString("autoid") != null? Integer.parseInt(resultSet.getString("autoid")) : null);
            	result.add(timekeepingSheet);
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

    public static void updateState(ApproveTimeKeeping approveTimeKeeping){
        Connection connection = null;
        Statement statement = null;
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            StringBuilder sqlBuilder = new StringBuilder();
            String approvalIds = "";
            if (!ObjectUtils.isEmpty(approveTimeKeeping.getApprovalIds()) && !ObjectUtils.isEmpty(approveTimeKeeping.getState())){
                for (Integer autoId : approveTimeKeeping.getApprovalIds()){
                    if (StringUtils.isEmpty(approvalIds)){
                        approvalIds = autoId.toString();
                    }else {
                        approvalIds += "," + autoId;
                    }
                }

                sqlBuilder.append("update timekeeping_sheet set state ='"+approveTimeKeeping.getState()+"', created_at = created_at ");

                if (!StringUtils.isEmpty(approveTimeKeeping.getApprovedBy())) {
                    sqlBuilder.append(", approved_by='"+approveTimeKeeping.getApprovedBy()+"'");
                }

                if (!StringUtils.isEmpty(approveTimeKeeping.getApprovedAt())) {
                    String approveAtValue = dateFormat.format(approveTimeKeeping.getApprovedAt());
                    sqlBuilder.append(", approved_at=STR_TO_DATE('"+approveAtValue+"', '%d/%m/%Y %T')");
                }

                if (!StringUtils.isEmpty(approveTimeKeeping.getApprovalByLevel2())) {
                    sqlBuilder.append(", approval_by_level2='"+approveTimeKeeping.getApprovalByLevel2()+"'");
                }

                if (!StringUtils.isEmpty(approveTimeKeeping.getApprovedAtLevel2())) {
                    String approveAtLevel2Value = dateFormat.format(approveTimeKeeping.getApprovedAtLevel2());
                    sqlBuilder.append(", approval_at_level2=STR_TO_DATE('"+approveAtLevel2Value+"', '%d/%m/%Y %T')");
                }

                sqlBuilder.append(" where autoid in ("+approvalIds+")");
            }

            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sqlBuilder.toString());

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }

    public static void updateApprovalLevel2ByIds(ApproveTimeKeeping approveTimeKeeping){
        Connection connection = null;
        Statement statement = null;
        try{
            StringBuilder sqlBuilder = new StringBuilder();
            String approvalIds = "";
            if (!ObjectUtils.isEmpty(approveTimeKeeping.getApprovalIds()) && !ObjectUtils.isEmpty(approveTimeKeeping.getState())){
                for (Integer autoId : approveTimeKeeping.getApprovalIds()){
                    if (StringUtils.isEmpty(approvalIds)){
                        approvalIds = autoId.toString();
                    }else {
                        approvalIds += "," + autoId;
                    }
                }

                sqlBuilder.append("update timekeeping_sheet set approval_by_level2 ='"+approveTimeKeeping.getApprovalByLevel2()+"', created_at = created_at " +
                        " where autoid in ("+approvalIds+")");
            }

            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sqlBuilder.toString());

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }

    public static void updateStateOneRecord(Integer autoid, String state){
        Connection connection = null;
        Statement statement = null;

        try{
            StringBuilder sqlBuilder = new StringBuilder();
            String approvalIds = "";
            if (!ObjectUtils.isEmpty(autoid) && !ObjectUtils.isEmpty(state)){

                sqlBuilder.append("update timekeeping_sheet set state ='"+state+"', created_at = created_at " +
                        " where autoid ="+autoid+" ");
            }

            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sqlBuilder.toString());

        } catch (Exception e){
            e.printStackTrace();
        }  finally {
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }

    public static void importExcel(ImportExcelDto importExcelDto){
        Connection connection = null;
        Statement statement = null;
        try{
            StringBuilder sqlBuilder = new StringBuilder();
            String approvalIds = "";
            if (!ObjectUtils.isEmpty(importExcelDto)){
                sqlBuilder.append("insert into import_excel (module,title,import_by,attachment,failrecords,totalrecords)" +
                        " values ('TIME_KEEPING_SHEET','"+importExcelDto.getTitle()+"','"+importExcelDto.getImportBy()+"','" +
                        importExcelDto.getAttachment()+"',"+importExcelDto.getFailrecords()+","+importExcelDto.getTotalrecords()+")");
            }

            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sqlBuilder.toString());

        } catch (Exception e){
            e.printStackTrace();
        }  finally {
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }

    public static void deleteTimeKeeping(Integer autoid){
        Connection connection = null;
        Statement statement = null;

        try{
            StringBuilder sqlBuilder = new StringBuilder();
            String approvalIds = "";
            if (!ObjectUtils.isEmpty(autoid) && !ObjectUtils.isEmpty(autoid)){

                sqlBuilder.append("delete from timekeeping_sheet " +
                        " where autoid ="+autoid+" ");
            }

            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(sqlBuilder.toString());

        } catch (Exception e){
            e.printStackTrace();
        }  finally {
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }

    public static List<ApprovalFlow> getApprovalFlowByRole(String role, Integer groupId, String approvalType){
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ApprovalFlow approvalFlow = null;
        List<ApprovalFlow> result = new ArrayList();
        try {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append(" select autoid, group_id, dep_id, approval_type, approval_level, approval_level1, " +
                    "approval_level2, approval_level3, created_at, created_by, modified_at from approval_flow ");
            sqlBuilder.append("where approval_level1 = '"+role+"' and approval_type = '"+approvalType+"' ");

//            if (!StringUtils.isEmpty(role) && (role.equals(Constants.RoleConstants.GROUP_LEADER) )){
            sqlBuilder.append(" and dep_id = "+groupId);
//            } else if (!StringUtils.isEmpty(role) && role.equals(Constants.RoleConstants.DEP_LEADER)) {
//                sqlBuilder.append(" and group_id = "+groupId);
//            }

//            if (!StringUtils.isEmpty(role) && (role.equals(Constants.RoleConstants.DEP_LEADER)
//                    || (role.equals(Constants.RoleConstants.GROUP_LEADER)))) {
//                sqlBuilder.append(" and group_id = " + groupId);
//            }

            sqlBuilder.append(" union select autoid, group_id, dep_id, approval_type, approval_level, approval_level1, " +
                    "approval_level2, approval_level3, created_at, created_by, modified_at from approval_flow ");
            sqlBuilder.append("where approval_level2 = '"+role+"' and approval_type = '"+approvalType+"' ");

            sqlBuilder.append(" and dep_id = "+groupId);

//            if (!StringUtils.isEmpty(role) && (role.equals(Constants.RoleConstants.GROUP_LEADER))){
//                sqlBuilder.append(" and dep_id = "+groupId);
//            } else if (!StringUtils.isEmpty(role) && role.equals(Constants.RoleConstants.DEP_LEADER)) {
//                sqlBuilder.append(" and group_id = "+groupId);
//            }

            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBuilder.toString());
            while (resultSet.next()) {
                approvalFlow = new ApprovalFlow();
                approvalFlow.setAutoid(Integer.parseInt(resultSet.getString("autoid")));
                approvalFlow.setGroupId(!StringUtils.isEmpty(resultSet.getString("group_id"))
                        ? Integer.parseInt(resultSet.getString("group_id")) : null);
                approvalFlow.setDepId(!StringUtils.isEmpty(resultSet.getString("dep_id"))
                        ? Integer.parseInt(resultSet.getString("dep_id")) : null);
                approvalFlow.setApprovalType(resultSet.getString("approval_type"));
                approvalFlow.setApprovalLevel(!StringUtils.isEmpty(resultSet.getString("approval_level"))
                        ? Integer.parseInt(resultSet.getString("approval_level")) : null);
                approvalFlow.setApprovalLevel1(!StringUtils.isEmpty(resultSet.getString("approval_level1"))
                        ? resultSet.getString("approval_level1") : null);
                approvalFlow.setApprovalLevel2(!StringUtils.isEmpty(resultSet.getString("approval_level2"))
                        ? resultSet.getString("approval_level2") : null);
                approvalFlow.setApprovalLevel3(!StringUtils.isEmpty(resultSet.getString("approval_level3"))
                        ? resultSet.getString("approval_level3") : null);

                result.add(approvalFlow);
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

    public static ApprovalFlow getApprovalFlowByGroupId(Integer groupId, String approvalType){
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        ApprovalFlow approvalFlow = null;
        try {
            StringBuilder sqlBuilder = new StringBuilder();

            sqlBuilder.append(" select autoid, group_id, dep_id, approval_type, approval_level, approval_level1, " +
                    "approval_level2, approval_level3, created_at, created_by, modified_at from approval_flow ");
            sqlBuilder.append("where group_id = "+groupId+" and approval_type ='"+approvalType+"'");


            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBuilder.toString());
            while (resultSet.next()) {
                approvalFlow = new ApprovalFlow();
                approvalFlow.setAutoid(Integer.parseInt(resultSet.getString("autoid")));
                approvalFlow.setGroupId(!StringUtils.isEmpty(resultSet.getString("group_id"))
                        ? Integer.parseInt(resultSet.getString("group_id")) : null);
                approvalFlow.setDepId(!StringUtils.isEmpty(resultSet.getString("dep_id"))
                        ? Integer.parseInt(resultSet.getString("dep_id")) : null);
                approvalFlow.setApprovalType(resultSet.getString("approval_type"));
                approvalFlow.setApprovalLevel(!StringUtils.isEmpty(resultSet.getString("approval_level"))
                        ? Integer.parseInt(resultSet.getString("approval_level")) : null);
                approvalFlow.setApprovalLevel1(!StringUtils.isEmpty(resultSet.getString("approval_level1"))
                        ? resultSet.getString("approval_level1") : null);
                approvalFlow.setApprovalLevel2(!StringUtils.isEmpty(resultSet.getString("approval_level2"))
                        ? resultSet.getString("approval_level2") : null);
                approvalFlow.setApprovalLevel3(!StringUtils.isEmpty(resultSet.getString("approval_level3"))
                        ? resultSet.getString("approval_level3") : null);

            }

            return approvalFlow;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        } finally {
            Utility.closeObject(resultSet);
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
    }
}
