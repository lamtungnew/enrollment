package com.lvt.khvip.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.entity.Role;
import com.lvt.khvip.entity.User;
import com.lvt.khvip.util.Utility;

public class LoginDAO {
	private static final Logger log = LoggerFactory.getLogger(LoginDAO.class);

	public static User validate(String username, String password) {

		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			connection = ConnectDB.getConnection();
			String sql = "select u.people_id, u.id, u.username , u.password , r.name, p.group_id, g.parent_id "
						+ "from Users u inner join User_role ur on u.id = ur.user_id "
						+ "			 	inner join roles r on ur.role_id  = r.id "
						+ " inner join people p on p.people_id = u.people_id "
						+ " inner join groups g on g.group_id = p.group_id "
						+ "where username = ? and password = ?";
			ps = connection.prepareStatement(sql);
			ps.setString(1, username);
			ps.setString(2, password);

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				Role role = new Role();
				role.setName(rs.getString("name"));
				User user = new User();
				user.setId(rs.getInt("id"));
				user.setPeopleId(rs.getString("people_id"));
				user.setRole(role);
				user.setManageGroupId(rs.getString("group_id")!= null ?
						Integer.parseInt(rs.getString("group_id")): null);
				user.setUsername(rs.getString("username"));
				user.setGroupId(rs.getInt("group_id"));
				user.setDepId(rs.getInt("parent_id"));
				return user;
			}
		} catch (SQLException ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			Utility.closeObject(resultSet);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
		return null;
	}
}