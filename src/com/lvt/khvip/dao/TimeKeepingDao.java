package com.lvt.khvip.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.constant.Constants;
import com.lvt.khvip.entity.People;
import com.lvt.khvip.entity.Timekeeping;
import com.lvt.khvip.util.Utility;
import com.lvt.khvip.util.Utils;

@SessionScoped
@ManagedBean
public class TimeKeepingDao implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(TimeKeepingDao.class);

    public static List<Timekeeping> getListTimekeepingOfPeople(String peopleId, int groupId, String fromDate, String toDate, boolean isMorningLate) {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Timekeeping timekeeping = null;
        List<Timekeeping> timekeepings = new ArrayList<>();
        try {
            
            String sql = " select t.*,  "
            		+ " 	trim(concat(if (morning_late < 0, CONCAT('Đi muộn ', abs(morning_late), ' phút. '),''), "
            		+ " 	if (t.noon_time is null, CONCAT('Không có giờ trưa. '),''), "
            		+ " 	if (t.early_leave > 0, CONCAT('Về sớm ', abs(early_leave), ' phút.'),''))) decription "
            		+ " from (SELECT  "
            		+ "    p.full_name AS full_name, "
            		+ "    CONCAT('" + Constants.URL_IMAGE + "', image_path) AS url, "
            		+ "    DATE_FORMAT(MIN(d.captured_time), '%H:%i:%s') AS check_in, "
            		+ "    DATE_FORMAT(max(d.day_noon_time), '%H:%i:%s') AS noon_time, "
            		+ "    DATE_FORMAT(MAX(d.captured_time), '%H:%i:%s') AS check_out, "
            		+ "    p.people_id AS people_id, "
            		+ "    g.group_id, "
            		+ "    g.group_name, "
            		+ "    DATE(d.captured_time) AS ngay_cham_cong, "
            		+ "    ROUND(TIMESTAMPDIFF(MINUTE, "
            		+ "                MIN(d.captured_time), "
            		+ "                MAX(d.captured_time)) / 60, "
            		+ "            2) AS tong_gio, "
            		+ "    TIMESTAMPDIFF(MINUTE, time(MIN(d.captured_time)), (select time(value) from config c where c.code = 'DAY_START' and c.status=1)) morning_late, "
            		+ "    TIMESTAMPDIFF(MINUTE, time(MAX(d.captured_time)), (select time(value) from config c where c.code = 'DAY_END' and c.status=1)) early_leave "
            		+ "FROM "
            		+ "    Detection d "
            		+ "        INNER JOIN "
            		+ "    People p ON p.people_id = d.people_id "
            		+ "        LEFT JOIN "
            		+ "    groups g ON p.group_id = g.group_id "
            		+ "GROUP BY d.people_id , DATE(d.captured_time) , p.full_name , p.image_path "
            		+ "ORDER BY DATE(d.captured_time) DESC, d.people_id  desc) t "
            		+ "WHERE 1=1 ";
            		
            if (!Utils.isEmpty(peopleId)) {
            	sql += "AND t.people_id like '%' || ? || '%' ";
            }
            if (groupId != 0) {
            	sql += "AND t.group_id = ? ";
            }
            if (!Utils.isEmpty(fromDate)) {
                sql += "AND t.ngay_cham_cong >= ? " ;
            }
            if (!Utils.isEmpty(toDate)) {
                sql += "AND t.ngay_cham_cong <= ? " ;
            }
            if (isMorningLate) {
                sql += "AND t.morning_late < 0 " ;
            }

            connection = ConnectDB.getConnection();
            ps = connection.prepareStatement(sql);
            
            int i = 1;
            if (!Utils.isEmpty(peopleId)) {
            	ps.setString(i++, peopleId);
            }
            if (groupId != 0) {
            	ps.setInt(i++, groupId);
            }
            if (!Utils.isEmpty(fromDate)) {
            	ps.setString(i++, fromDate);
            }
            if (!Utils.isEmpty(toDate)) {
            	ps.setString(i++, toDate);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                timekeeping = new Timekeeping();
                timekeeping.setFullName(rs.getString("full_name"));
                timekeeping.setImagePath(rs.getString("url"));
                timekeeping.setCheckIn(rs.getString("check_in"));
                timekeeping.setNoonTime(rs.getString("noon_time"));
                timekeeping.setCheckOut(rs.getString("check_out"));
                timekeeping.setPeopleId(rs.getString("people_id"));
                timekeeping.setDateTimeKeeping(rs.getString("ngay_cham_cong"));
                timekeeping.setTotalWork(rs.getString("tong_gio"));
                timekeeping.setGroupName(rs.getString("group_name"));
                timekeeping.setMorningLate(rs.getString("morning_late"));
                timekeeping.setEarlyLeave(rs.getString("early_leave"));
                timekeeping.setDecription(rs.getString("decription"));
                timekeepings.add(timekeeping);
            }
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        } finally {
            Utility.closeObject(rs);
            Utility.closeObject(ps);
            Utility.closeObject(connection);
        }
        return timekeepings;
    }

    public static List<Timekeeping> getListTimekeeping() {
        Connection connection = null;
        Statement statement = null;
        ResultSet rs = null;
        Timekeeping timekeeping = null;
        List<Timekeeping> timekeepings = new ArrayList<>();
        try {
            String sql = " select t.*,  "
            		+ " 	trim(concat(if (morning_late < 0, CONCAT('Đi muộn ', abs(morning_late), ' phút. '),''), "
            		+ " 	if (t.noon_time is null, CONCAT('Không có giờ trưa. '),''), "
            		+ " 	if (t.early_leave > 0, CONCAT('Về sớm ', abs(early_leave), ' phút.'),''))) decription "
            		+ " from (SELECT  "
            		+ "    p.full_name AS full_name, "
            		+ "    CONCAT('" + Constants.URL_IMAGE + "', image_path) AS url, "
            		+ "    DATE_FORMAT(MIN(d.captured_time), '%H:%i:%s') AS check_in, "
            		+ "    DATE_FORMAT(max(d.day_noon_time), '%H:%i:%s') AS noon_time, "
            		+ "    DATE_FORMAT(MAX(d.captured_time), '%H:%i:%s') AS check_out, "
            		+ "    p.people_id AS people_id, "
            		+ "    g.group_name, "
            		+ "    DATE(d.captured_time) AS ngay_cham_cong, "
            		+ "    ROUND(TIMESTAMPDIFF(MINUTE, "
            		+ "                MIN(d.captured_time), "
            		+ "                MAX(d.captured_time)) / 60, "
            		+ "            2) AS tong_gio, "
            		+ "    TIMESTAMPDIFF(MINUTE, time(MIN(d.captured_time)), (select time(value) from config c where c.code = 'DAY_START' and c.status=1)) morning_late, "
            		+ "    TIMESTAMPDIFF(MINUTE, time(MAX(d.captured_time)), (select time(value) from config c where c.code = 'DAY_END' and c.status=1)) early_leave "
            		+ "FROM "
            		+ "    Detection d "
            		+ "        INNER JOIN "
            		+ "    People p ON p.people_id = d.people_id "
            		+ "        LEFT JOIN "
            		+ "    groups g ON p.group_id = g.group_id "
            		+ "GROUP BY d.people_id , DATE(d.captured_time) , p.full_name , p.image_path "
            		+ "ORDER BY DATE(d.captured_time) DESC, d.people_id  desc "
            		+ "LIMIT 1000 ) t ";
            connection = ConnectDB.getConnection();
            statement = connection.createStatement();
            rs = statement.executeQuery(sql);
            while (rs.next()) {
                timekeeping = new Timekeeping();
                timekeeping.setFullName(rs.getString("full_name"));
                timekeeping.setImagePath(rs.getString("url"));
                timekeeping.setCheckIn(rs.getString("check_in"));
                timekeeping.setNoonTime(rs.getString("noon_time"));
                timekeeping.setCheckOut(rs.getString("check_out"));
                timekeeping.setPeopleId(rs.getString("people_id"));
                timekeeping.setDateTimeKeeping(rs.getString("ngay_cham_cong"));
                timekeeping.setTotalWork(rs.getString("tong_gio"));
                timekeeping.setGroupName(rs.getString("group_name"));
                timekeeping.setMorningLate(rs.getString("morning_late"));
                timekeeping.setEarlyLeave(rs.getString("early_leave"));
                timekeeping.setDecription(rs.getString("decription"));
                timekeepings.add(timekeeping);
            }
        } catch (Exception e) {
        	log.error(e.getMessage(), e);
        } finally {
            Utility.closeObject(rs);
            Utility.closeObject(statement);
            Utility.closeObject(connection);
        }
        return timekeepings;
    }
    
	public static List<People> getPeopeIdList(String peopleId) {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<People> peopleIdList = new ArrayList<>();
		try {
			String sql = " SELECT  "
            		+ "    people_id, "
            		+ "    CONCAT('" + Constants.URL_IMAGE + "',image_path) AS url, "
            		+ "    full_name, "
            		+ "    g.group_name "
            		+ "FROM "
            		+ "    People p "
            		+ "        LEFT JOIN "
            		+ "    groups g ON p.group_id = g.group_id "
            		+ "    where p.status = 1 ";
			if (!Utils.isEmpty(peopleId)) {
            	sql += "AND p.people_id like '%' || ? || '%' ";
            }
			
			connection = ConnectDB.getConnection();
			ps = connection.prepareStatement(sql);
			if (!Utils.isEmpty(peopleId)) {
				ps.setString(1, peopleId);
			}
			
			rs = ps.executeQuery();
			while (rs.next()) {
				People people = new People();
				people.setPeopleId(rs.getString("people_id"));
				people.setImagePath(rs.getString("url"));
				people.setFullName(rs.getString("full_name"));
				people.setGroupName(rs.getString("group_name"));
				peopleIdList.add(people);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		} finally {
			Utility.closeObject(rs);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
		return peopleIdList;
	}
}
