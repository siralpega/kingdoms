package com.siralpega.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import com.siralpega.Kingdoms.Kingdoms;

public class SQLGetTask extends SQLTask 
{
	private String idCol;
	private Object id, result;
	private String searchCol;
	private Callback<Object> callback;

	public SQLGetTask(Connection con, String table, String[] tableColumns, String idCol, Object id, String searchCol, Callback<Object> cb)
	{
		super(con, table, tableColumns);
		this.idCol = idCol;
		this.id = id;
		this.searchCol = searchCol;
		this.callback = cb;
	}

	@Override
	public void run()
	{
		try
		{
			String sql = "SELECT * FROM " + getTable() + " WHERE " + idCol + " = ?";
			PreparedStatement statement = getConnection().prepareStatement(sql);

			if(id instanceof String)
				statement.setString(1, (String) id);
			else
				statement.setInt(1, (Integer) id);
			ResultSet results = statement.executeQuery();
			if(!results.next()) 
				result = null;
			else 
				result = results.getObject(searchCol);
			
			 new BukkitRunnable()
	            {
	                @Override
	                public void run()
	                {
	                    callback.execute(result);
	                }
	            }.runTask(Kingdoms.getInstance());
			
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
