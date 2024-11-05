package com.lvt.khvip.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lvt.khvip.entity.CustomerType;
import com.lvt.khvip.util.Utility;

public class CustomerTypeDao {
	private static final Logger log = LoggerFactory.getLogger(CustomerTypeDao.class);

	public List<CustomerType> getCustomerTypeList() {
		Connection connection = null;
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		List<CustomerType> customerTypeList = new ArrayList<CustomerType>();
		CustomerType customerType = null;
		try {
			connection = ConnectDB.getConnection();
			String sql = " SELECT  "
					+ "    id, name "
					+ "FROM "
					+ "    customertype "
					+ "WHERE "
					+ "    status = 1 ";
			ps = connection.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				customerType = new CustomerType();
				customerType.setCustomerTypeId(rs.getInt("id"));
				customerType.setCustomerTypeName(rs.getString("name"));
				customerTypeList.add(customerType);
			}
		} catch (SQLException ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			Utility.closeObject(resultSet);
			Utility.closeObject(ps);
			Utility.closeObject(connection);
		}
		return customerTypeList;
	}
}