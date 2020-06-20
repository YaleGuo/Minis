package com.minis.jdbc.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public abstract class OldJdbcTemplate {
	public OldJdbcTemplate() {
	}
	
	public Object query(String sql) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		Object rtnObj = null;
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

			stmt = con.prepareStatement(sql);
			rs = stmt.executeQuery();
			
			rtnObj = doInStatement(rs);
		}
		catch (Exception e) {
				e.printStackTrace();
		}
		finally {
			try {
				rs.close();
				stmt.close();
				con.close();
			} catch (Exception e) {
				
			}
		}
		
		return rtnObj;

	}
	protected abstract  Object doInStatement(ResultSet rs);
}
