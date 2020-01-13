package com.siralpega.Kingdoms.Towns;


import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.siralpega.Kingdoms.Kingdoms;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import net.md_5.bungee.api.ChatColor;

public class CommandTown implements CommandExecutor 
{

	private final Kingdoms plugin;
	public CommandTown(Kingdoms instance) 
	{
		this.plugin = instance;	
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(command.getLabel().equals("addtown"))
		{
			if(args.length <= 2)
			{
				try 
				{
					addTown(sender, args);
				}
				catch (SQLException | IncompleteRegionException | CircularInheritanceException e) 
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Failed to add. Tell an admin.");
					if(e instanceof SQLException)
						Bukkit.broadcast(plugin.prefix + ChatColor.RED + "ERROR: Database failure! Plugin and database are no longer in sync", "kingdoms.admin");
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /addtown <name> <team>");
		}
		else if(command.getLabel().equalsIgnoreCase("removetown"))
		{
			if(args.length <= 1)
			{
				try
				{
					removeTown(sender, args);
				}
				catch(SQLException | IOException e)
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Failed to remove. Tell an admin");
					if(e instanceof SQLException)
						Bukkit.broadcast(plugin.prefix + ChatColor.RED + "ERROR: Database failure! Plugin and database are no longer in sync", "kingdoms.admin");
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /removetown [name]");
		}
		else if(command.getLabel().equalsIgnoreCase("towninfo"))
		{
			if(args.length <= 1)
			{
				try
				{
					getTownInfo(sender, args);
				}
				catch(Exception e)
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Failed to get town info. Tell an admin");
					if(e instanceof SQLException)
						Bukkit.broadcast(plugin.prefix + ChatColor.RED + "ERROR: Database failure! Plugin and database are no longer in sync", "kingdoms.admin");
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /towninfo [name]");
		}
		else if(command.getLabel().equalsIgnoreCase("buildtown"))
		{
			if(sender instanceof Player)
			{
				Player p  = (Player) sender;
				p.getInventory().addItem(TownBuilder.getTownBuilderItem());
				p.sendMessage(plugin.prefix + "Left click with Town Placer to create town");
			}
			else
				sender.sendMessage(plugin.prefix + "Error: Must be a player to build a town");
		}
		else
			return false;
		return true;
	}
	
	/**
	 * @deprecated 
	 * <b>WARNING: Used for staff to MANUALLY add towns. Players use TownListener, SignEditorListener & TownBuilder
	 */
	public void addTown(CommandSender sender, String[] args) throws SQLException, IncompleteRegionException, CircularInheritanceException
	{
		//NOTE: This method if for creating a town manually. The "Town Placer" uses TownListener & TownBuilder
		// /addtown <name> <team>
		if(args.length == 0 || args[0] == null || args[1] == null || args[0].isEmpty() || args[1].isEmpty())
		{
			sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Args for addtown were null/empty");
			return;
		}
		if(sender instanceof Player)
		{
			if(plugin.getRegionManager().addRegion((Player) sender, "town", args[0]))
			{
				plugin.getDBManager().addTown(args[0].toLowerCase(), args[1].toLowerCase(), KingdomsRegionManager.region_prefix.concat(args[0]).toLowerCase());
				sender.sendMessage(plugin.prefix + ChatColor.GREEN + "Town " + ChatColor.AQUA + args[0] + ChatColor.GREEN + " created!");
				plugin.getLogger().info("Town " + args[0] + " was added to the " + args[1] + " team by " + sender.getName());
			}
		}
		else if(args.length == 3)
		{
			plugin.getRegionManager().addRegion((Player) sender, "town", args[0]);
		}
	}

	private void removeTown(CommandSender sender, String[] args) throws SQLException, IOException
	{
		ProtectedRegion region = null;
		String id = "";

		if(args.length == 0 && sender instanceof Player)
		{
			Player p = (Player) sender;
			id = plugin.getRegionManager().findTownOrPlot(p.getLocation());
			if(id == null)
			{
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Not standing in valid town. Move into one or try /removetown <town name>");
				return;
			}
			id = id.substring(id.indexOf("_") + 1);
			if(id.contains("_plot_"))
				id = id.substring(0, id.indexOf("_plot_"));
			Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " " + plugin.prefix_json
					+ "{\"text\":\"Are you sure you want to delete \",\"color\":\"green\"},{\"text\":\"" + id + "\",\"color\":\"aqua\"},{\"text\":\"?\",\"color\":\"green\"},"
					+ "{\"text\":\" [CONFIRM]\",\"bold\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/removetown " + id + "\"}}]");
			return;
		}
		id = KingdomsRegionManager.region_prefix.concat(args[0]);
		region =  plugin.getRegionManager().getRegion(id);
		if(region == null)
		{
			sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Can't find region with name " + id);
			return;
		}
		if(region.getParent() != null)
		{
			sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Cannot remove a plot with /removetown. Try /removeplot");
			return;
		}
		//Remove town & plots
		ApplicableRegionSet rgs = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(Bukkit.getServer().getWorld("world"))).getApplicableRegions(region);
		Iterator<ProtectedRegion> it = rgs.iterator();
		while(it.hasNext())
		{
			plugin.getRegionManager().removeRegion(it.next().getId());
		}
		id = id.substring(id.indexOf("_") + 1);
		plugin.getDBManager().removeTown(id);
		sender.sendMessage(plugin.prefix + "Town " + ChatColor.AQUA + id + ChatColor.GREEN + " and all plots were removed!");
		plugin.getLogger().info("Town " + id + " was removed by " + sender.getName());
	}

	private void getTownInfo(CommandSender sender, String[] args) throws SQLException
	{
		String town = "";
		if(args.length == 0 && sender instanceof Player)
		{
			Player p = (Player) sender;
			town = plugin.getRegionManager().findTownOrPlot(p.getLocation());
			if(town == null)
			{
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Not standing in valid town. Move into one or try /towninfo <town name>");
				return;
			}
			town = town.substring(town.indexOf("_") + 1);
		}
		else if(args.length >= 1)
			town = args[0];
		else
		{
			sender.sendMessage("Incorrect syntax.");
			return;
		}
		if(town.contains("_plot_"))
			town = town.substring(0, town.indexOf("_plot_"));
		
		plugin.getDBManager().getTownInfo(sender, town);
	}
}
