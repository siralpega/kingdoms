package com.github.siralpega.Kingdoms.Towns;


import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.github.siralpega.Kingdoms.Kingdoms;
import com.github.siralpega.util.MinecraftReflector;
import com.github.siralpega.util.SignEditor;
import com.github.siralpega.util.SignEditorListener;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import net.md_5.bungee.api.ChatColor;


public class TownListener implements Listener
{
	private final Kingdoms plugin;
	private PlotBuilder pb;
	public TownListener(Kingdoms instance)
	{
		this.plugin = instance;
		pb = new PlotBuilder(plugin);
	}

	@EventHandler
	public void onClick(PlayerInteractEvent event) throws IOException, InvalidConfigurationException, CircularInheritanceException, SQLException
	{
		if(event.getAction() == Action.LEFT_CLICK_BLOCK)
		{
			Block block = event.getClickedBlock();
			if(block.getType() == Material.BOOKSHELF)
			{
				if(plugin.getRegionManager().isInTownBoundary((BlockVector3.at(event.getPlayer().getLocation().getBlockX(),event.getPlayer().getLocation().getBlockY(),event.getPlayer().getLocation().getBlockZ()))))
					//	TownManager.givePlayerResources(event.getPlayer());
					event.setCancelled(true);
			}
			else if(event.getPlayer().getInventory().getItemInMainHand().equals(TownBuilder.getTownBuilderItem()))
			{
				event.setCancelled(true);
				block.setType(Material.OAK_SIGN);
				Sign sign = (Sign) block.getState();
				SignEditor se = new SignEditor(new MinecraftReflector());
				se.commit(event.getPlayer(), sign);
				new SignEditorListener(event.getPlayer(), plugin);
			}
			else if(event.getPlayer().getInventory().getItemInMainHand().equals(PlotBuilder.getPlotBuilderItem()))
			{
				Player p = event.getPlayer();
				String regionStanding = plugin.getRegionManager().findTownOrPlot(p.getLocation());
				if(regionStanding == null)
				{
					p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Not in a town");
					return;
				}
				if(regionStanding.contains("_plot_")) 
				{
					p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Can't build a plot overlapping an exisiting plot");
					return;
				}
				ProtectedRegion region = plugin.getRegionManager().getRegion((regionStanding));
				if(region == null)
				{
					p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Not in a town");
					return;
				}
				if(!region.getMembers().contains(p.getUniqueId()))
				{
					p.sendMessage(plugin.prefix + ChatColor.RED +  "Error: You don't have permission to create plots in " + regionStanding.substring(regionStanding.indexOf("_") + 1));
					return;
				}
				event.setCancelled(true);
				Location loc = pb.playerAddPlot(p.getUniqueId(), event.getClickedBlock().getLocation());
				if(loc != null)
				{
					String flag = "";
					float yaw = p.getLocation().getYaw();
					if(yaw >= 45 && yaw < 135) //west
						flag = "west";
					else if(yaw >= 135 && yaw < 225) //north
						flag = "north";
					else if(yaw >= 225 && yaw < 315) //east
						flag = "east";
					else if(yaw >= 315 || yaw < 45) //south
						flag = "south";

					try 
					{
						if(plugin.getRegionManager().addRegionRedux(p, loc, event.getClickedBlock().getLocation(), "plot", regionStanding, flag))
						{
							plugin.getDBManager().addPlot(regionStanding.substring(regionStanding.indexOf("_") + 1), 1);
							p.sendMessage(plugin.prefix + ChatColor.GREEN + "A plot in " + ChatColor.AQUA + regionStanding.substring(regionStanding.indexOf("_") + 1) + ChatColor.GREEN + " was created!");
							plugin.getLogger().info("A plot was added to Town " + regionStanding + " by " + p.getName());
							p.getInventory().remove(PlotBuilder.getPlotBuilderItem());
						}
						else
							p.sendMessage(plugin.prefix + "Error: Couldn't create plot. Perhaps it is overlapping a plot, or out of the bounds of the town?");
					} catch (IncompleteRegionException e) {
						p.sendMessage(plugin.prefix + " Error: Region could not be created. Tell an admin.");
						e.printStackTrace();
					}	
				}
				else
					p.sendMessage(plugin.prefix + "Position 1 set");
			}
		}
	}
}

