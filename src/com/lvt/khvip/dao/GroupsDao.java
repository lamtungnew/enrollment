package com.lvt.khvip.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.entity.Groups;
import com.lvt.khvip.util.Utility;


public class GroupsDao implements Serializable{
	private static final Logger log = LoggerFactory.getLogger(GroupsDao.class);

	public List<Groups> getGroupList() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<Groups> groupList = new ArrayList<Groups>();
		Groups group = null;
		try {
			connection = ConnectDB.getConnection();
			String sql = " SELECT "
					+ "    group_id, group_code, group_name "
					+ "FROM "
					+ "    groups "
					+ "WHERE "
					+ "    status = 1 ";
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				group = new Groups();
				group.setGroupId(rs.getInt("group_id"));
				group.setGroupCode(rs.getString("group_code"));
				group.setGroupName(rs.getString("group_name"));
				groupList.add(group);
			}
		} catch (SQLException ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			Utility.closeObject(resultSet);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
		return groupList;
	}



}