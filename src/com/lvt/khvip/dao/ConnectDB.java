package com.lvt.khvip.dao;

import java.sql.Connection;
import java.sql.SQLException;

import com.lvt.khvip.util.ConfProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectDB {
	private final static HikariConfig config = new HikariConfig();
	private final static HikariDataSource ds;

	static {
		config.setJdbcUrl(ConfProperties.getProperty("db.connection.url"));
		config.setUsername(ConfProperties.getProperty("db.username"));
		config.setPassword(ConfProperties.getProperty("db.password"));
		config.setDriverClassName(ConfProperties.getProperty("db.driver"));
		config.addDataSourceProperty("cachePrepStmts", ConfProperties.getProperty("db.cachePrepStmts"));
		config.addDataSourceProperty("prepStmtCacheSize", ConfProperties.getProperty("db.prepStmtCacheSize"));
		config.addDataSourceProperty("prepStmtCacheSqlLimit", ConfProperties.getProperty("db.prepStmtCacheSqlLimit"));
		ds = new HikariDataSource(config);
	}

	public static Connection getConnection() throws SQLException {
		return ds.getConnection();
	}
}
