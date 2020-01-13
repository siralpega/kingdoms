package com.siralpega.Kingdoms.Towns;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import com.siralpega.Kingdoms.Kingdoms;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import net.md_5.bungee.api.ChatColor;

public class CommandPlots implements CommandExecutor 
{

	private final Kingdoms plugin;
	private Map<Player, String> plotInvites;
	public CommandPlots(Kingdoms instance) 
	{
		this.plugin = instance;	
		plotInvites = new HashMap<Player, String>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(command.getLabel().equals("addplot"))
		{
			if(args.length <= 1)
			{
				try 
				{
					addPlot(sender, args);
				} 
				catch (IncompleteRegionException | SQLException | CircularInheritanceException e) 
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Failed to add. Tell an admin.");
					if(e instanceof SQLException)
						Bukkit.broadcast(plugin.prefix + ChatColor.RED + "ERROR: Database failure! Plugin and database are no longer in sync", "kingdoms.admin");
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /addplot [town name]");
		}
		else if(command.getLabel().equalsIgnoreCase("removeplot"))
		{
			if(args.length <= 2)
			{
				try
				{
					removePlot(sender, args);
				}
				catch(SQLException | IOException e)
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Failed to remove. Tell an admin.");
					if(e instanceof SQLException)
						Bukkit.broadcast(plugin.prefix + ChatColor.RED + "ERROR: Database failure! Plugin and database are no longer in sync", "kingdoms.admin");
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /removeplot [town name] [id]");
		}
		else if(command.getLabel().equalsIgnoreCase("plotinvite"))
		{
			if(args.length == 1)
			{
				try
				{
					plotInvite(sender, args);
				}
				catch(Exception e)
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Failed to invite. Tell an admin.");
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /plotinvite <username>");
		}
		else if(command.getLabel().equalsIgnoreCase("plotjoin"))
		{
			if(args.length == 1)
			{
				try
				{
					plotJoin(sender, args);
				}
				catch(IOException | SQLException e)
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Failed to join. Tell an admin.");
					if(e instanceof SQLException)
						Bukkit.broadcast(plugin.prefix + ChatColor.RED + "ERROR: Database failure! Plugin and database are no longer in sync", "kingdoms.admin");
					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /plotjoin <region>");
		}
		else if(command.getLabel().equalsIgnoreCase("plotleave"))
		{
			try
			{
				leavePlot(sender);
			}
			catch(IOException e)
			{
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Failed to leave. Tell an admin.");
				e.printStackTrace();
			}
		}
		else if(command.getLabel().equalsIgnoreCase("buildplot"))
		{
			if(sender instanceof Player)
			{
				Player p  = (Player) sender;
				p.getInventory().addItem(PlotBuilder.getPlotBuilderItem());
				p.sendMessage(plugin.prefix + "Left click with Plot Placer to select two points for a plot");
			}
			else
				sender.sendMessage(plugin.prefix + "Error: Must be a player to build a plot");
		}

		return true;
	}

	/**
	 * @deprecated 
	 * <b>WARNING: Used for staff to MANUALLY add plots. Players use TownListener & PlotBuilder
	 */
	private void addPlot(CommandSender sender, String[] args) throws IncompleteRegionException, SQLException, CircularInheritanceException
	{
		//addplot [town name]
		String town = "";
		if(sender instanceof Player)
		{
			Player p = (Player) sender;
			if(args.length == 0)
			{
				town = plugin.getRegionManager().findTownOrPlot(p.getLocation());
				if(town == null)
				{
					p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Not in a town & town name arg is empty");
					return;
				}
			}
			else
				town = KingdomsRegionManager.region_prefix.concat(args[0]);
			if(town.indexOf("town_") == -1)
			{
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Town " + town + " is not a region");
				return;
			}
			if(plugin.getRegionManager().addRegion(p, "plot", town))
			{
				plugin.getDBManager().addPlot(town.substring(town.indexOf("_") + 1), 1);
				p.sendMessage(plugin.prefix + ChatColor.GREEN + "A plot in " + ChatColor.AQUA + town.substring(town.indexOf("_") + 1) + ChatColor.GREEN + " was created!");
				plugin.getLogger().info("A plot was added to Town " + town + " by " + p.getName());
			}
		}
		else
			sender.sendMessage("Error: Command can only be executed by player (but can be changed easily)");
	}

	private void removePlot(CommandSender sender, String[] args) throws SQLException, IOException
	{
		///removeplot [town name] [id]
		if(sender instanceof Player)
		{
			Player p = (Player) sender;
			String rg = "";
			if(args.length == 0)
			{
				rg = plugin.getRegionManager().findTownOrPlot(p.getLocation());
				if(rg == null || !rg.contains("_plot_"))
				{
					p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Not standing in valid plot. Move into one or try /removeplot <town name> <id>");
					return;
				}
			}
			else if(args.length == 1)
			{
				p.sendMessage(plugin.prefix + ChatColor.RED + "Missing id. Try standing in a plot or /removeplot [town name] [id]");
				return;
			}
			else
			{
				rg = KingdomsRegionManager.region_prefix.concat(args[0]);
				rg = rg.concat("_plot_" + args[1]);
			}

			ProtectedRegion region = plugin.getRegionManager().getRegion(rg);
			if(region == null)
			{
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Could not find region " + rg);
				return;
			}
			if(region.getParent() == null)
			{
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Can't delete a town with /removeplot. Try /removetown");
				return;
			}
			//Remove plot
			if(!plugin.getRegionManager().removeRegion(region.getId()))
			{
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Could not remove region " + region.getId());
				return;
			}

			String town = rg.substring(rg.indexOf("_") + 1, rg.indexOf("_plot_"));
			plugin.getDBManager().addPlot(town, -1);
			sender.sendMessage(plugin.prefix + "Plot " + ChatColor.AQUA + rg.substring(rg.lastIndexOf("_") + 1) + ChatColor.GREEN + " removed from " + ChatColor.AQUA + town);
			plugin.getLogger().info("Plot " + region.getId() + " was removed by " + p.getName());
		}
		else
			sender.sendMessage("Error: Command can only be executed by player (but can be changed easily)");
	}

	private void plotInvite(CommandSender sender, String[] args) 
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("Error: must be send by player");
			return;
		}
		Player p = (Player) sender, target = Bukkit.getPlayer(args[0]);
		//WARNING: we are assuming that a player can only be part of ONE plot
		String plot = plugin.getRegionManager().getPlayerPlot(p);
		if(plot == null || plot.equalsIgnoreCase(""))
		{
			p.sendMessage(plugin.prefix + ChatColor.RED + "You aren't part of a plot");
			return;
		}
		if(target == null)
		{
			p.sendMessage(plugin.prefix + ChatColor.RED + "Player " + args[0] + " was not found");
			return;
		}

		//TODO: Check if sender has permission to invite players to their plot (a plot mod/owner)
		int x = new BukkitRunnable()
		{
			@Override
			public void run()
			{
				target.sendMessage(plugin.prefix + "Invitation to join a plot from " + ChatColor.AQUA + p.getName() + ChatColor.GREEN + " has expired");
				plotInvites.remove(target);
			}
		}.runTaskLaterAsynchronously(plugin, 20l*600l).getTaskId(); //20l*600l
		plotInvites.put(target, x + "__" + plot);

		String town = plot.substring(plot.indexOf("_") + 1, plot.indexOf("_plot_"));
		target.sendMessage(plugin.prefix + ChatColor.AQUA + p.getName() + ChatColor.GREEN + " invited you to join their plot in " + ChatColor.AQUA + town);
		Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + target.getName() + " " + 
				"{\"text\":\"    [ACCEPT]\",\"bold\":true,\"color\":\"green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/plotjoin " + plot + "\"}}]");
	}

	private void plotJoin(CommandSender sender, String[] args) throws IOException, SQLException
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("Error: must be send by player");
			return;
		}
		Player p = (Player) sender;
		String regionName = args[0];
		if(!regionName.contains("_plot_") || !regionName.contains("town_"))
		{
			sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Incorrect region id.");
			return;
		}
		if(!plotInvites.containsKey(p))
		{
			sender.sendMessage(plugin.prefix + ChatColor.RED + "You haven't been invited to join a plot");
			return;
		}
		String currPlot = plugin.getRegionManager().getPlayerPlot(p);
		if(currPlot != null && !currPlot.equalsIgnoreCase(""))
		{
			plugin.getRegionManager().removeMember(p, plugin.getRegionManager().getRegion(currPlot));
			p.sendMessage(plugin.prefix + "You have left " + ChatColor.AQUA +  currPlot);
		}
		String plot = plotInvites.get(p);
		int taskToCancel = Integer.parseInt(plot.substring(0, plot.indexOf("__")));
		plot = plot.substring(plot.indexOf("__") + 2);
		plugin.getRegionManager().addMember(p, plugin.getRegionManager().getRegion(plot));
		p.sendMessage(plugin.prefix + "You have joined " + ChatColor.AQUA +  plugin.getRegionManager().getRegion(plot).getId());
		plotInvites.remove(p);
		Bukkit.getServer().getScheduler().cancelTask(taskToCancel);
	}

	private void leavePlot(CommandSender sender) throws IOException
	{
		if(!(sender instanceof Player))
		{
			sender.sendMessage("Error: must be send by player");
			return;
		}
		Player p = (Player) sender;
		String plot = plugin.getRegionManager().getPlayerPlot(p);
		if(plot == null || plot.equalsIgnoreCase(""))
		{
			p.sendMessage(plugin.prefix + ChatColor.RED + "You aren't part of a plot");
			return;
		}
		plugin.getRegionManager().removeMember(p, plugin.getRegionManager().getRegion(plot));
		p.sendMessage(plugin.prefix + "You have left " + ChatColor.AQUA +  plot);
	}
}
