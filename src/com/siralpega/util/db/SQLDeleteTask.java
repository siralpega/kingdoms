package com.siralpega.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SQLDeleteTask extends SQLTask
{
	private String idCol;
	private Object id;

	public SQLDeleteTask(Connection con, String table, String[] tableColumns, String idCol, Object id)
	{
		super(con, table, tableColumns);
		this.idCol = idCol;
		this.id = id;
	}
	@Override
	public void run()
	{
		try
		{
		//	if(!SQLManager.getInstance().contains(getTable(), idCol, id))
		//		return;
			if(!isConnected())
				throw new SQLException("Not connected to database");
			PreparedStatement st = getConnection().prepareStatement("DELETE FROM " + getTable() + " WHERE " + idCol + " = ?");
			if(id instanceof String)
				st.setString(1, (String) id);
			else
				st.setInt(1, (Integer) id);
			st.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}