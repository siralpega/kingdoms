package com.github.siralpega.Kingdoms;

import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.github.siralpega.util.db.Callback;
import com.github.siralpega.util.db.SQLTask.UpdateType;

import net.md_5.bungee.api.ChatColor;

public class KingdomsDB
{
	private Kingdoms plugin;
	@SuppressWarnings("unused")
	private String townTable, otherTable;
	public KingdomsDB(Kingdoms instance, String one, String two) 
	{
		plugin = instance;	
		townTable =  one;
		otherTable = two;
	}

	public void getPlotTest(String town) //this would be in plugin specfic class
	{
		Callback<Object> callback = new Callback<Object>()
		{
			public void execute(Object o)
			{
				Bukkit.broadcastMessage("# of plots is " + (int) o);
			}
		};

		plugin.getSQLManager().getASyncValue(townTable, "name", town, "plots", callback);
	}

	public void addTown(String town_name, String team, String region) throws SQLException
	{
		Callback<Object> callback = new Callback<Object>()
		{
			public void execute(Object o)
			{
				if(o == null) //if o is not null, then it is found and we dont want to insert!
					plugin.getSQLManager().insert(townTable, town_name, team, region, 0, 0, 0, 0, 0);
				//table, town, team, region, plots, citizens (members), resources, defense, utilityPlots
			}
		};
		plugin.getSQLManager().getASyncValue(townTable, "name", town_name, "plots", callback);
	}

	public void removeTown(String town) throws SQLException
	{
		plugin.getSQLManager().delete(townTable, "name", town);
	}

	private void changeValue(String town, UpdateType type, String column, Object value) throws SQLException
	{
		Callback<Object> callback = new Callback<Object>()
		{
			public void execute(Object o)
			{
				if(o != null) //if o is null, then the db doesn't contain town!
					plugin.getSQLManager().update(townTable, type, "name", town, column, value);
			}
		};
		plugin.getSQLManager().getASyncValue(townTable, "name", town, "plots", callback);
	}

	public void addPlot(String town, int add) throws SQLException
	{
		changeValue(town, UpdateType.ADD, "plots", add);
	}

	public void addMember(String town, int add) throws SQLException
	{
		changeValue(town, UpdateType.ADD, "members", add);
	}

	public void addResource(String town, int add) throws SQLException
	{
		changeValue(town, UpdateType.ADD, "resources", add);
	}

	public void addDefense(String town, int add) throws SQLException
	{
		changeValue(town, UpdateType.ADD, "defense", add);
	}

	public void addUtilPlot(String town, int add) throws SQLException
	{
		changeValue(town, UpdateType.ADD, "utilPlots", add);
	}

	public void getTownInfo(CommandSender sender, String townName)
	{
		Callback<Object> callback = new Callback<Object>()
		{

			public void execute(Object response)
			{
				if(response == null) //if response is null,then town not found;
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Could not find town " + townName);
					return;
				}
				String town, team, region;
				int plots, members, level = 0, utils, resources, defense;
				Object[] results = (Object[]) response;
				town = (String) results[0];
				team = (String) results[1];
				region = (String) results[2];
				plots = (int) results[3];
				members = (int) results[4];
				resources = (int) results[5];
				defense = (int) results[6];
				utils = (int) results[7];
				ChatColor color;
				try
				{
					if(team.toUpperCase().equals("PURPLE"))
						team = "LIGHT_PURPLE";
					color = ChatColor.valueOf(team.toUpperCase());
				}
				catch(Exception e)
				{
					color = ChatColor.AQUA;
				}
				sender.sendMessage(ChatColor.GREEN + "==========[" + ChatColor.AQUA + town + ChatColor.GREEN + "]=========");
				sender.sendMessage(ChatColor.GREEN + "Team: " + color + team
						+ ChatColor.GREEN + ", Region: " + ChatColor.AQUA + region
						+ ChatColor.GREEN + ", Level: " + ChatColor.AQUA + level);
				sender.sendMessage(ChatColor.GREEN + "Members: " + ChatColor.AQUA + members
						+ ChatColor.GREEN + ", Plots: " + ChatColor.AQUA + plots
						+ ChatColor.GREEN + ", Utilities: " + ChatColor.AQUA + utils);
				sender.sendMessage(ChatColor.GREEN + "Resources: " + ChatColor.AQUA + resources
						+ ChatColor.GREEN + ", Defense: " + ChatColor.AQUA + defense);
			}
		};	
		plugin.getSQLManager().getASyncValues(townTable, "name", townName, callback);
	}
}
