/**
 *
 */
package com.lvt.khvip.dao;

import com.lvt.khvip.entity.ApproveTimeKeeping;
import com.lvt.khvip.entity.OtSheetDate;
import com.lvt.khvip.util.StringUtils;
import com.lvt.khvip.util.Utility;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: tuha.lvt
 *
 * @date:
 */

@ManagedBean
@SessionScoped
public class OtSheetDao implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(OtSheetDao.class);

    public static void deleteOt(Integer autoid){
        Connection connection = null;
        Statement statement = null;

        try{
            StringBuilder sqlBuilder = new StringBuilder();
            String approvalIds = "";
            if (!ObjectUtils.isEmpty(autoid) && !ObjectUtils.isEmpty(autoid)){

                sqlBuilder.append("delete from ot_sheet " +
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

                sqlBuilder.append("update ot_sheet set approval_by_level2 ='"+approveTimeKeeping.getApprovalByLevel2()+"', created_at = created_at " +
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

                sqlBuilder.append("update ot_sheet set state ='"+approveTimeKeeping.getState()+"', created_at = created_at ");

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
}
