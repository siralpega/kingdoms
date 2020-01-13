/************************
 *  Kingdoms
 *  AUTHOR: Alpega_
 *  Version: 1
 *  CAT: Gameplay
 *  DESC: Foundation for Kingdoms gameplay
 ************************/
package com.siralpega.Kingdoms;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import com.siralpega.Kingdoms.Towns.CommandPlots;
import com.siralpega.Kingdoms.Towns.CommandStructure;
import com.siralpega.Kingdoms.Towns.CommandTown;
import com.siralpega.Kingdoms.Towns.KingdomsRegionManager;
import com.siralpega.Kingdoms.Towns.StructureBuild;
import com.siralpega.Kingdoms.Towns.StructureManager;
import com.siralpega.Kingdoms.Towns.TownListener;
import com.siralpega.util.db.SQLManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StringFlag;

public class Kingdoms extends JavaPlugin
{
	private static Kingdoms instance;
	public String prefix, prefix_json;
	private SQLManager sql;
	private KingdomsDB db;
	private KingdomsRegionManager krm;
	private StructureManager sm;
	public static StringFlag DIRECTION_FLAG = new StringFlag("direction");

	/*
	 * TODO
	 * Clean up town creation/remove process. Currently it is all over the place. Need to doc it and make it better centralized/clear/not repeating
	 * Structures:
	 *	 - After town reaches next level (figure that out), then upgrade util plots
	 * 	 - Structure functions: get resources after input? over time? Certain skills only? [util plots]
	 * Skills
	 * Allow a town owner/admin to sell/give plots to players. OR: create a buy function for a plot not owned: owner of town gets money/sets price?
	 * Add /plotinfo (copy /towninfo?)
	 * Make it so that a plot created w/ dimensions less than minimum in PlotBuilder will NOT be created (boolean)
	 * Make it so that overlapping doesn't give out two errors (return after first) and it won't build the plot
	 * fix/re-do player resources
	 * make it so town resources/defense are calculated by formula. make it so a member is added when somebody joins the town/plot
	 * if a util plot, set it in db
	 */

	public void onLoad()
	{
		Collection<String> col = new ArrayList<String>();
		col.add("north");
		col.add("west");
		col.add("south");
		col.add("east");
		DIRECTION_FLAG.chooseValue(col);
		WorldGuard.getInstance().getFlagRegistry().register(DIRECTION_FLAG);
	}

	@Override
	public void onEnable() 
	{
		instance = this;
		prefix = ChatColor.GREEN + "[" + ChatColor.AQUA + "K" + ChatColor.GREEN + "] ";
		prefix_json = "[\"\",{\"text\":\"[\",\"color\":\"green\"},{\"text\":\"K\",\"color\":\"aqua\"},{\"text\":\"] \",\"color\":\"green\"},";
		//Commands
		CommandTown ct = new CommandTown(this);
		CommandPlots pt = new CommandPlots(this);
		CommandSkills st = new CommandSkills(this);
		CommandStructure struct = new CommandStructure(this);
		getCommand("addtown").setExecutor(ct);
		getCommand("removetown").setExecutor(ct);
		getCommand("towninfo").setExecutor(ct);
		getCommand("addplot").setExecutor(pt);
		getCommand("removeplot").setExecutor(pt);
		getCommand("plotinvite").setExecutor(pt);
		getCommand("plotjoin").setExecutor(pt);
		getCommand("plotleave").setExecutor(pt);
		getCommand("skills").setExecutor(st);
		getCommand("setskill").setExecutor(st);
		getCommand("struct").setExecutor(struct);
		getCommand("buildtown").setExecutor(ct);
		getCommand("buildplot").setExecutor(pt);
		//Listeners
		getServer().getPluginManager().registerEvents(new TownListener(this), this);
		//Other
		new Skills(this);
		//Managers
		sm = new StructureManager(this, new StructureBuild(this));
		krm = new KingdomsRegionManager(this);
		db = new KingdomsDB(this, "towns", "other");
		//SQL - always load last
		this.getLogger().info("Loading config & connecting to database");
		sql = new SQLManager(this, false);	
	}

	@Override
	public void onDisable() 
	{	
		try 
		{
			if(sql.connection != null && !sql.connection.isClosed())
				sql.connection.close();
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}

	public void loadConfig()
	{
		getConfig().options().copyDefaults(true);
		saveConfig();
	}

	public KingdomsDB getDBManager()
	{
		return db;
	}

	public SQLManager getSQLManager()
	{
		return sql;
	}

	public KingdomsRegionManager getRegionManager()
	{
		return krm;
	}

	public StructureManager getStructureManager()
	{
		return sm;
	}

	public static Kingdoms getInstance()
	{
		return instance;
	}

}
