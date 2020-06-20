package com.test.service;

import java.sql.ResultSet;

import com.minis.beans.factory.annotation.Autowired;
import com.minis.jdbc.core.JdbcTemplate;
import com.test.entity.User;

public class UserService {
	@Autowired
	JdbcTemplate jdbcTemplate;
	
//	public User getUserInfo(int userid) {
//		final String sql = "select id, name,birthday from users where id="+userid;
//		return (User)jdbcTemplate.query(
//				(stmt)->{			
//					ResultSet rs = stmt.executeQuery(sql);
//					User rtnUser = null;
//					if (rs.next()) {
//						rtnUser = new User();
//						rtnUser.setId(userid);
//						rtnUser.setName(rs.getString("name"));
//						rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
//					} else {
//					}
//					return rtnUser;
//				}
//		);
//	}
	
	public User getUserInfo(int userid) {
		final String sql = "select id, name,birthday from users where id=?";
		return (User)jdbcTemplate.query(sql, new Object[]{new Integer(userid)},
				(pstmt)->{			
					ResultSet rs = pstmt.executeQuery();
					User rtnUser = null;
					if (rs.next()) {
						rtnUser = new User();
						rtnUser.setId(userid);
						rtnUser.setName(rs.getString("name"));
						rtnUser.setBirthday(new java.util.Date(rs.getDate("birthday").getTime()));
					} else {
					}
					return rtnUser;
				}
		);
	}
}
