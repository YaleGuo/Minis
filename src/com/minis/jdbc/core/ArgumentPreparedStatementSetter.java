package com.minis.jdbc.core;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ArgumentPreparedStatementSetter {
	private final Object[] args;

	public ArgumentPreparedStatementSetter(Object[] args) {
		this.args = args;
	}


	public void setValues(PreparedStatement pstmt) throws SQLException {
		if (this.args != null) {
			for (int i = 0; i < this.args.length; i++) {
				Object arg = this.args[i];
				doSetValue(pstmt, i + 1, arg);
			}
		}
	}

	protected void doSetValue(PreparedStatement pstmt, int parameterPosition, Object argValue) throws SQLException {

			if (argValue instanceof String) {
				pstmt.setString(parameterPosition, (String)argValue);
			}
			else if (argValue instanceof Integer) {
				pstmt.setInt(parameterPosition, (int)argValue);
			}
			else if (argValue instanceof java.util.Date) {
				pstmt.setDate(parameterPosition, new java.sql.Date(((java.util.Date)argValue).getTime()));
				
			}

	}

}
