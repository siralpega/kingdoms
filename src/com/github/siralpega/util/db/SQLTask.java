package com.github.siralpega.util.db;

import java.sql.Connection;
import java.sql.SQLException;
import org.bukkit.scheduler.BukkitRunnable;

public abstract class SQLTask extends BukkitRunnable
{
	private Connection connection;
	private String[] columns;
	private String table;
	public enum UpdateType {REPALCE, ADD};

	/**
	 * BASIC CONSTRUCTOR
	 */
	public SQLTask(Connection con, String table, String[] columns)
	{
		connection = con;
		this.table = table;
		this.columns = columns;
	}

	protected String getTable()
	{
		return table;
	}

	protected String[] getColumns()
	{
		return columns;
	}

	protected Connection getConnection() throws SQLException
	{
		if(isConnected())
			return connection;
		throw new SQLException("Not connected to DB!");
	}

	protected boolean isConnected() throws SQLException
	{
		if(connection != null)
			return !connection.isClosed();
		return false;
	}

	@Override
	public void run() 
	{	

	}
}
