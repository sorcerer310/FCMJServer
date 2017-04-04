package com.rafo.chess.common.db;


import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public final class MySQLManager {

	private static BoneCP connectionPools;
	private static MySQLManager datasource = new MySQLManager();

	private MySQLManager() {
	}

	public void init(Properties props) {
		if(connectionPools == null){
			try {
				Class.forName(props.getProperty("local.jdbc.driver"));
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			BoneCPConfig config = new BoneCPConfig();
			config.setJdbcUrl(props.getProperty("local.jdbc.url"));
			config.setUsername(props.getProperty("local.jdbc.user"));
			config.setPassword(props.getProperty("local.jdbc.password"));
			config.setMinConnectionsPerPartition(30);
			config.setMaxConnectionsPerPartition(60);
			config.setPartitionCount(1);

			try {
				connectionPools = new BoneCP(config);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public Connection getConnection() {
		try {
			return connectionPools.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
			return  null;
		}
	}

	public void closeConnection(Connection conn){
		if (conn != null)
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
	}


	public static MySQLManager getInstance() {
		return datasource;
	}

	public static void close(ResultSet rs, Statement st, Connection conn) {
		try {
			if (rs != null)
				rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			try {
				if (st != null)
					st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			} finally {
				if (conn != null)
					try {
						conn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
			}
		}
	}

}