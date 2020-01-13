package com.siralpega.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.siralpega.Kingdoms.Kingdoms;

public class SQLUpdateTask extends SQLTask
{
	private String idCol;
	private Object id;
	private String updateColumn;
	private Object value;
	private UpdateType op;

	public SQLUpdateTask(Connection con, String table, String[] tableColumns, String idCol, Object id, String updateColumn, Object value, UpdateType type)
	{
		super(con, table, tableColumns);
		this.idCol = idCol;
		this.id = id;
		this.updateColumn = updateColumn;
		this.value = value;
		this.op = type;
	}

	@Override
	public void run()
	{
		try
		{
			if(op == UpdateType.ADD && value instanceof Integer)
			{
				add();
				return;
			}
			String sql = "UPDATE " + getTable() + " set " + updateColumn + " = ? WHERE " + idCol + " = ?";
			PreparedStatement statement = getConnection().prepareStatement(sql);
			if(value instanceof Integer)
				statement.setInt(1, (Integer) value);
			else if(value instanceof String)
				statement.setString(1, (String) value);

			if(id instanceof Integer)
				statement.setInt(2, (Integer) id);
			else
				statement.setString(2, (String) id);

			statement.executeUpdate();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}

	private void add()
	{
		Callback<Object> callback = new Callback<Object>()
		{
			public void execute(Object o)
			{
				try 
				{
					String sql = "UPDATE " + getTable() + " set " + updateColumn + " = ? WHERE " + idCol + " = ?";
					PreparedStatement statement = getConnection().prepareStatement(sql);
					statement.setInt(1, (Integer) value + (Integer) o);

					if(id instanceof Integer)
						statement.setInt(2, (Integer) id);
					else
						statement.setString(2, (String) id);
					statement.executeUpdate();
				}
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
			}
		};
		Kingdoms.getInstance().getSQLManager().getASyncValue(getTable(), idCol, id, updateColumn, callback);
	}
}
