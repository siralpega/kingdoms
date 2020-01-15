package com.github.siralpega.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLInsertTask extends SQLTask
{
	private Object[] values;

	public SQLInsertTask(Connection con, String table, String[] tableColumns, Object[] values)
	{
		super(con, table, tableColumns);
		this.values = values;
	}

	@Override
	public void run()
	{
		if(getTable().isEmpty() || values.length == 0)
			return;
		String sql = "INSERT INTO " + getTable() + " (";
		String sqlend = "?";
		String[] columns = getColumns();
		if(columns.length != values.length)
			return;
		sql += columns[0] + "";
		for(int i = 1; i < values.length; i++)
		{
			sql += "," + columns[i];
			sqlend += ",?";
		}
		sql += ") VALUES (" + sqlend + ")";
		try
		{
			PreparedStatement statement = getConnection().prepareStatement(sql);
			for(int i = 1; i <= values.length; i++)
			{

				if(values[i - 1] instanceof String)
					statement.setString(i, values[i - 1].toString());
				else if(values[i - 1] instanceof Integer)
					statement.setInt(i, (Integer) values[i - 1]);
				else
					return;
			}
			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
