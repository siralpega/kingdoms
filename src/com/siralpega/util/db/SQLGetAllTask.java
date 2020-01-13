package com.siralpega.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.scheduler.BukkitRunnable;

import com.siralpega.Kingdoms.Kingdoms;

public class SQLGetAllTask extends SQLTask 
{
	private String idCol;
	private Object id;
	private Object[] resultsArray;
	private String[] multiCols;
	private Callback<Object> callback;

	public SQLGetAllTask(Connection con, String table, String[] tableColumns, String idCol, Object id, Callback<Object> cb)
	{
		super(con, table, tableColumns);
		this.idCol = idCol;
		this.id = id;
		this.callback = cb;
		this.multiCols = tableColumns;
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
					return;
				else 
				{
					resultsArray = new Object[multiCols.length];
					for(int i = 0; i < multiCols.length; i++)
						resultsArray[i] = results.getObject(multiCols[i]);
				}
			
			new BukkitRunnable()
			{
				@Override
				public void run()
				{
					callback.execute(resultsArray);
				}
			}.runTask(Kingdoms.getInstance());

		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
}
