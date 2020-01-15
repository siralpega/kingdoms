package com.github.siralpega.util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.github.siralpega.Kingdoms.Kingdoms;
import com.github.siralpega.util.db.SQLTask.UpdateType;
/**
 * API for SQL operations
 * @author Alpega_
 */
public class SQLManager
{
	private Kingdoms plugin;
	private static SQLManager instance;

	private String host, database, username, password;
	private Map<String, String[]> tableColumns;
	private int port;
	public Connection connection;
	private boolean printStack;

	public SQLManager(Kingdoms instance, boolean printErrors)
	{
		plugin = instance;	
		tableColumns = new HashMap<String, String[]>();
		printStack = printErrors;
		setupConnection();
		SQLManager.instance = this;
	}
	/**
	 * Start a connection to a database. Gets parameters from plugin's config file.
	 */
	public void setupConnection()
	{
		plugin.loadConfig();
		this.host = plugin.getConfig().getString("sql.host");
		this.port = plugin.getConfig().getInt("sql.port");
		this.database = plugin.getConfig().getString("sql.database");
		this.username = plugin.getConfig().getString("sql.username");
		this.password = plugin.getConfig().getString("sql.password");
		List<String> tables = plugin.getConfig().getStringList("sql.tables");
		for(int i = 0; i < tables.size(); i++)
			tableColumns.put(tables.get(i), null);

		new BukkitRunnable()
		{
			@Override
			public void run() {
				try {
					openConnection();
					setupTables(tables);
				} 
				catch (SQLException e) {
					if(printStack)
						e.printStackTrace();
				}
				new BukkitRunnable()
				{
					@Override
					public void run() {
						try {
							if(connection == null || connection.isClosed())
								plugin.getLogger().info("ERROR! Can't connect to database!");
						} catch (SQLException e) {
							if(printStack)
								e.printStackTrace();
						}	
					}
				}.runTask(plugin);
			}	
		}.runTaskAsynchronously(plugin);
	}

	private void openConnection() throws SQLException
	{
		if(connection != null && !connection.isClosed())
			return;

		synchronized(this)
		{
			if(connection != null && !connection.isClosed())
				return;
			//		Class.forName("com.mysqp.jdbc.Driver");
			connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password);
		}
	}

	private void setupTables(List<String> tables) throws SQLException
	{
		for(int i = 0; i < tableColumns.size(); i++)
		{
			String table = tables.get(i);
			tableColumns.put(table, getTableCols(table));
		}
	}

	private String[] getTableCols(String table_name) throws SQLException
	{
		String[] cols;
		PreparedStatement s = connection.prepareStatement("SELECT * FROM " + table_name);
		ResultSet rs = s.executeQuery();
		ResultSetMetaData meta = rs.getMetaData();
		int length = meta.getColumnCount();
		cols = new String[length];
		for(int colNum = 1; colNum <= length; colNum++)
			cols[colNum - 1] = meta.getColumnName(colNum); 
		return cols;
	}



	//Example of using a task inside a async task
	public void sendMessageFromDB(Player p, final String table_name, final String column)
	{
		new BukkitRunnable()
		{
			@Override
			public void run() 
			{
				try
				{
					PreparedStatement s = connection.prepareStatement("SELECT * FROM " + table_name + " WHERE " + column + " = ?");
					s.setString(1, "yes");
					ResultSet rs = s.executeQuery();
					if(!rs.next())
						return;
					final String yes = rs.getString(column);

					new BukkitRunnable()
					{

						@Override
						public void run()
						{
							p.sendMessage(yes);
						}
					}.runTask(Kingdoms.getInstance());
				}
				catch(SQLException e)
				{

				}
			}
		}.runTaskAsynchronously(Kingdoms.getInstance());

	}
	/**
	 * OPERATIONS
	 **/

	/** 
	 * Inserts into table TABLE in a new row with VALUES. The amount and type of values must match the table's columns. 
	 * WARNING: will not check if already existing. Use update() to change existing row.
	 * @param table the db table
	 * @param values the values to be inserted
	 * @throws SQLException
	 */
	public void insert(String table, Object...values)
	{
		SQLInsertTask t = new SQLInsertTask(connection, table, tableColumns.get(table), values);
		t.runTaskAsynchronously(plugin);
	}

	/** 
	 * Updates VALUE in COLUMN in a row that has ID in column IDCOL
	 * @param table db table name
	 * @param type UpdateType.REPLACE or UpdateType.ADD. If skipped, will default to replace
	 * @param idCol the column that our id is part of
	 * @param id the id used to search (find row)
	 * @param column the column where the value should be updated
	 * @param value the value to be updated
	 * @throws SQLException
	 */
	public void update(String table, UpdateType type, String idCol, Object id, String column, Object value)
	{
		SQLUpdateTask t = new SQLUpdateTask(connection, table, tableColumns.get(table), idCol, id, column, value, type);
		t.runTaskAsynchronously(plugin);
	}

	/** 
	 * Deletes a row in TABLE where ID is in IDCOL
	 * @param table the db table
	 * @param idCol the column where id is present
	 * @param id uses to identify what row to delete
	 * @throws SQLException
	 */
	public void delete(String table, String col, String id) 
	{
		Callback<Object> callback = new Callback<Object>()
		{
			public void execute(Object o)
			{
				if(o != null) //if o is null, then the db doesn't contain town!
				{
					SQLDeleteTask t = new SQLDeleteTask(connection, table, tableColumns.get(table), col, id);
					t.runTaskAsynchronously(plugin);
				}
			}
		};
		plugin.getSQLManager().getASyncValue(table, col, id, col, callback);	
	}

	/** 
	 * Gets a value in the db. WARNING: must be called through a callback
	 * @param table the db table
	 * @param idCol the column where id is present
	 * @param id used to find what row we are talking about
	 * @param searchCol the column to search for the value
	 * @param cb the callback object
	 * @throws SQLException
	 */
	public void getASyncValue(String table, String idCol, Object id, String searchCol, Callback<Object> cb)
	{
		SQLGetTask t = new SQLGetTask(connection,table, tableColumns.get(table), idCol, id, searchCol, cb);
		t.runTaskAsynchronously(plugin);	
	}

	/**
	 * Gets all the values of a row that is specified by id. WARNING: must be called through a callback
	 * @param table
	 * @param idCol
	 * @param id
	 * @param cb
	 */
	public void getASyncValues(String table, String idCol, Object id, Callback<Object> cb)
	{
		SQLGetAllTask t = new SQLGetAllTask(connection,table, tableColumns.get(table), idCol, id,cb);
		t.runTaskAsynchronously(plugin);	
	}

	public static SQLManager getInstance()
	{
		return instance;
	}
}
