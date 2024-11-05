/**
 *
 */
package com.lvt.khvip.dao;

import com.google.gson.Gson;
import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.entity.TimekeepingSheet;
import com.lvt.khvip.entity.TimekeepingDate;
import com.lvt.khvip.util.StringUtils;
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
public class TimekeepingDateDao implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(TimekeepingDateDao.class);

    public static List<TimekeepingDate> getListTimeKeepingDate(TimekeepingDate condition) {
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;
        TimekeepingDate timekeepingSheet = null;
        List<TimekeepingDate> result = new ArrayList();
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String createdAtStr = "";
            
            sqlBuilder.append("select autoid, date from timekeeping_date ");
            sqlBuilder.append("order by date");
            
            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sqlBuilder.toString());
            while (resultSet.next()) {
            	timekeepingSheet = new TimekeepingDate();
            	timekeepingSheet.setAutoid(resultSet.getInt("autoid"));
            	timekeepingSheet.setDate(resultSet.getString("date"));
            	            	
            	result.add(timekeepingSheet);
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
