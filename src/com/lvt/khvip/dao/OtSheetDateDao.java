/**
 *
 */
package com.lvt.khvip.dao;

import com.lvt.khvip.entity.OtSheetDate;
import com.lvt.khvip.util.Utility;
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
public class OtSheetDateDao implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(OtSheetDateDao.class);

    public static List<OtSheetDate> getListOtSheetDate(OtSheetDate condition) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        OtSheetDate otSheetDate = null;
        List<OtSheetDate> result = new ArrayList();
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String createdAtStr = "";
            
            sqlBuilder.append("select autoid, ot_date, start_time, to_time, ot_id from timekeeping_date ");
            sqlBuilder.append("order by date");
            
            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBuilder.toString());
            while (resultSet.next()) {
            	otSheetDate = new OtSheetDate();
            	otSheetDate.setAutoid(resultSet.getInt("autoid"));
            	otSheetDate.setOtDate(resultSet.getString("ot_date"));
                otSheetDate.setStartTime(resultSet.getString("start_time"));
                otSheetDate.setToTime(resultSet.getString("to_time"));
                otSheetDate.setOtId(Integer.parseInt(resultSet.getString("ot_id")));

                result.add(otSheetDate);
            }

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
