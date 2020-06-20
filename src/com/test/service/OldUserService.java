package com.test.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.test.entity.User;

public class OldUserService {
	public User getUserInfo(int userid) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		User returnUser =null;
		
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			con = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databasename=DEMO;user=sa;password=Sql2016;");

			stmt = con.prepareStatement("select name,birthday from users where id=?");
			stmt.setInt(1, userid);
			rs = stmt.executeQuery();
			
			if (rs.next()) {
				returnUser = new User();
				returnUser.setId(userid);
				returnUser.setName(rs.getString("name"));
				returnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
			} else {
			}
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
		
		return returnUser;
	}
}
