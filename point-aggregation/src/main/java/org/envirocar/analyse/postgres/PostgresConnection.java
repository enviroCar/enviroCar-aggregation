/**
 * Copyright (C) 2014 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.envirocar.analyse.postgres;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.envirocar.analyse.properties.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgresConnection {

	private static final Logger logger = LoggerFactory.getLogger(PostgresConnection.class);
	
	private String connectionURL = null;
	private String databaseName;
	private String databasePath;
															
	private String username;
	private String password;

	private Connection connection;
	
	public PostgresConnection() {
		try {
			createConnection();
		} catch (ClassNotFoundException e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private boolean createConnection() throws ClassNotFoundException {
		Class.forName("org.postgresql.Driver");
		connectionURL = "jdbc:postgresql:" + getDatabasePath() + "/"
				+ getDatabaseName();

		java.util.Properties props = new java.util.Properties();

		props.setProperty("create", "true");
		props.setProperty("user", getDatabaseUsername());
		props.setProperty("password", getDatabasePassword());
		this.connection = null;
		try {
			connection = DriverManager.getConnection(connectionURL, props);
			connection.setAutoCommit(false);
			logger.info("Connected to measurement database.");
		} catch (SQLException e) {
			logger.error("Could not connect to or create the database.", e);
			return false;
		}

		return true;
	}
	
	private String getDatabaseName() {
		
		if(databaseName == null || databaseName.equals("")){			
			this.databaseName = Properties.getProperty("databaseName").toString();
		}
		
		return databaseName;
	}

	private String getDatabasePath() {
		
		if(databasePath == null || databasePath.equals("")){			
			databasePath = Properties.getProperty("databasePath").toString();
		}
		
		return databasePath;
	}
	
	private String getDatabaseUsername() {
		
		if(username == null || username.equals("")){			
			username = Properties.getProperty("username").toString();
		}
		
		return username;
	}
	
	private String getDatabasePassword() {
		
		if(password == null || password.equals("")){
			this.password = Properties.getProperty("password").toString();
		}
		
		return password;
	}

	public boolean executeStatement(String statement) {
		Statement st = null;
		try {
			st = connection.createStatement();
			st.execute(statement);

		} catch (SQLException e) {
			logger.error("Execution of the following statement failed: "
					+ statement + " cause: " + e.getMessage());
			return false;
		} finally {
			try {
				connection.commit();
			} catch (SQLException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return true;
	}

	public ResultSet executeQueryStatement(String statement) {
		Statement st = null;
		try {
			st = connection.createStatement();
			ResultSet resultSet = st.executeQuery(statement);

			return resultSet;

		} catch (SQLException e) {
			logger.error("Execution of the following statement failed: "
					+ statement + " cause: " + e.getMessage());
			return null;
		} finally {
			try {
				connection.commit();
			} catch (SQLException e) {
				logger.warn(e.getMessage(), e);
			}
		}
	}

	public boolean executeUpdateStatement(String statement) {
		Statement st = null;
		try {
			st = connection.createStatement();
			st.executeUpdate(statement);
			
		} catch (SQLException e) {
			logger.error("Execution of the following statement failed: "
					+ statement + " cause: " + e.getMessage());
			return false;
		} finally {
			try {
				connection.commit();
				st.close();
			} catch (SQLException e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return true;
	}

	public DatabaseMetaData getDatabasMetaData() throws SQLException {
		return this.connection.getMetaData();
	}

	public PreparedStatement createPreparedStatement(String statement,
			int autoGeneratedKeys, List<Object> values) throws SQLException {
		PreparedStatement result = this.connection.prepareStatement(statement, autoGeneratedKeys);
		
		int index = 1;
		for (Object object : values) {
			if (object instanceof String) {
				result.setString(index++, object.toString());
			}
			else if (object instanceof Integer) {
				result.setInt(index++, (int) object);
			}
			else if (object instanceof Double) {
				result.setDouble(index++, (double) object);
			}
		}
		
		return result;
	}	
	
	
}
