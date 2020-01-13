package com.siralpega.Kingdoms.Towns;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import com.siralpega.Kingdoms.Kingdoms;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import net.md_5.bungee.api.ChatColor;

/* TownManager
 * Manages various tasks associated with Towns
 * 
 * 
 */
public class KingdomsRegionManager
{
	private final Kingdoms plugin;
	public static final String region_prefix = "town_";
	private RegionManager regions;
	private RegionContainer container;

	public KingdomsRegionManager(Kingdoms instance) 
	{
		plugin = instance;
		container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		regions = container.get(BukkitAdapter.adapt(Bukkit.getServer().getWorld("world")));
	}

	public ProtectedRegion getRegion(String id)
	{
		try
		{
			if (regions != null && regions.hasRegion(id)) 
				return regions.getRegion(id);
		}
		catch(Exception e)
		{
			//not in region
		}
		return null;
	}


	public boolean addRegion(Player p, String type, String id) throws IncompleteRegionException, SQLException, CircularInheritanceException
	{
		return addRegionRedux(p, null, null, type, id, "north");
	}

	public boolean addRegionRedux(Player p, Location first, Location second, String type, String id, String flag) throws IncompleteRegionException, SQLException, CircularInheritanceException
	{
		ProtectedRegion r;
		//Create a region for a town
		if(type.equalsIgnoreCase("town"))
		{
			id = region_prefix.concat(id);
			if(first == null || second == null) //using old player-based WE selection
				r = createRegion(p, id);
			else
				r = createRegion(p, first, second, id); //using new two location system
		}
		else
		{
			if(id.contains("_plot_")) //if the region is a plot, just get the parent (remove the plot id)
				id = id.substring(0, id.indexOf("_plot_"));
			ApplicableRegionSet rgs = regions.getApplicableRegions(plugin.getRegionManager().getRegion(id));
			int index = -1;
			Iterator<ProtectedRegion> regs = rgs.iterator();
			if(rgs.iterator().hasNext())
				index = 0;
			while(regs.hasNext())
			{
				regs.next();
				index++;
			}
			if(index < 0)
			{
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Can't create a plot id. Is database broken?");
				return false;
			}
			id = id.concat("_plot_" + index);
			if(first == null || second == null) //using old player-based WE selection
				r = createRegion(p, id);
			else
				r = createRegion(p, first, second, id); //using new two location system
			r.setFlag(Kingdoms.DIRECTION_FLAG, flag);
		}

		if(r == null)
			return false;
		ApplicableRegionSet rgs = regions.getApplicableRegions(r);
		if(type.equalsIgnoreCase("plot") && (rgs.size() <= 1 || rgs.size() > 2))
		{
			regions.removeRegion(r.getId());
			if(rgs.size() <= 1)
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Plot is not inside a town");
			else
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Plot cannot be inside another plot");
			return false;
		}
		else if(type.equalsIgnoreCase("plot") && rgs.iterator().hasNext())
		{
			Iterator<ProtectedRegion> parents = rgs.iterator();
			ProtectedRegion parent = parents.next();
			while(parent.getId().contains("_plot_") && parents.hasNext())
				parent = parents.next();
			r.setParent(parent);
		}
		else if(type.equalsIgnoreCase("town") && rgs.size() > 1)
		{
			regions.removeRegion(r.getId());
			p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Town cannot be inside another town");
			return false;
		}
		return true;
	}

	/*	public boolean addRegion(Player p, String type, String id) throws IncompleteRegionException, SQLException, CircularInheritanceException
	{
		ProtectedRegion r;
		//Create a region for a town
		if(type.equalsIgnoreCase("town"))
		{
			id = region_prefix.concat(id);
			r = createRegion(p, id);
		}
		//Create a region for a plot
		else
		{
			if(id.contains("_plot_")) //if the region is a plot, just get the parent (remove the plot id)
				id = id.substring(0, id.indexOf("_plot_"));
			int pid = plugin.getKingdomsDB().getTotalPlots(id);
			if(pid < 0)
			{
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Can't create a plot id. Is database broken?");
				return false;
			}
			id = id.concat("_plot_" + pid);
			r = createRegion(p, id);
		}
		if(r == null)
			return false;
		ApplicableRegionSet rgs = regions.getApplicableRegions(r);
		if(type.equalsIgnoreCase("plot") && (rgs.size() <= 1 || rgs.size() > 2))
		{
			regions.removeRegion(r.getId());
			if(rgs.size() <= 1)
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Plot is not inside a town");
			else
				p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Plot cannot be inside another plot");
			return false;
		}
		else if(type.equalsIgnoreCase("plot") && rgs.iterator().hasNext())
		{
			Iterator<ProtectedRegion> parents = rgs.iterator();
			ProtectedRegion parent = parents.next();
			while(parent.getId().contains("_plot_") && parents.hasNext())
				parent = parents.next();
			r.setParent(parent);
		}
		else if(type.equalsIgnoreCase("town") && rgs.size() > 1)
		{
			regions.removeRegion(r.getId());
			p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Town cannot be inside another town");
			return false;
		}
		return true;
	} */

	//Create a WG region using WE selection
	private ProtectedRegion createRegion(Player p, String id) throws IncompleteRegionException
	{
		LocalSession ses = WorldEdit.getInstance().getSessionManager().findByName(p.getDisplayName());
		if(ses == null || ses.getSelection(ses.getSelectionWorld()) == null)
		{
			p.sendMessage(plugin.prefix + ChatColor.RED + "Error: No selection made. Type //wand and select two blocks");
			return null;
		}
		Region playerSelection = ses.getSelection(ses.getSelectionWorld());
		Location first = new Location(Bukkit.getWorld("world"),playerSelection.getMaximumPoint().getBlockX(),playerSelection.getMaximumPoint().getBlockY(), playerSelection.getMaximumPoint().getBlockZ());
		Location second = new Location(Bukkit.getWorld("world"),playerSelection.getMinimumPoint().getBlockX(),playerSelection.getMinimumPoint().getBlockY(), playerSelection.getMinimumPoint().getBlockZ());
		ses.clearHistory();
		return createRegion(p, first, second, id);
	}

	//Create a WG region using two locations
	private ProtectedRegion createRegion(Player p, Location first, Location second, String id) throws IncompleteRegionException
	{
		if(regions == null)
		{
			container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			regions = container.get(BukkitAdapter.adapt(Bukkit.getServer().getWorld("world")));
		}
		if(first == null || second == null)
		{
			p.sendMessage(plugin.prefix + ChatColor.RED + "Error: Please left-click the town placer on a block");
			return null;
		}
		BlockVector3 r1 = BlockVector3.at(first.getBlockX(), first.getBlockY(), first.getBlockZ()).add(0, 40, 0);
		BlockVector3 r2 = BlockVector3.at(second.getBlockX(), second.getBlockY(), second.getBlockZ()).add(0, -20, 0);

		int overIndex = 0;
		while(regions.getRegion(id) != null) //if region already exists, we don't want to overwrite it!
			id = id.substring(0, id.lastIndexOf("_") + 1).concat(overIndex++ + "");

		ProtectedRegion region = new ProtectedCuboidRegion(id, r1, r2);
		region.setFlag(Flags.GREET_MESSAGE, "entering " + id);
		regions.addRegion(region);
		return region;
	}

	public boolean removeRegion(String name) throws IOException
	{
		ProtectedRegion region = getRegion(name);
		if(region != null)
		{
			container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			regions = container.get(BukkitAdapter.adapt(Bukkit.getServer().getWorld("world")));
			Set<UUID> set = region.getMembers().getUniqueIds();
			while(set.iterator().hasNext())
			{
				removeMember(Bukkit.getPlayer(set.iterator().next()), region);
			}
			regions.removeRegion(name);
			return true;
		}
		return false;
	}

	public boolean isInTownBoundary(BlockVector3 loc)
	{
		container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		regions = container.get(BukkitAdapter.adapt(Bukkit.getServer().getWorld("world")));
		ApplicableRegionSet rgs = regions.getApplicableRegions(loc);
		if(rgs.size() == 0)
			return false;
		return true;
	}
	/*
	public static void givePlayerResources(Player p) throws IOException, InvalidConfigurationException
	{
		container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		regions = container.get(BukkitAdapter.adapt(Bukkit.getServer().getWorld("world")));
		BlockVector3 loc = BlockVector3.at(p.getLocation().getBlockX(), p.getLocation().getBlockY(), p.getLocation().getBlockZ());
		ApplicableRegionSet rgs = regions.getApplicableRegions(loc);
		if(rgs.size() != 0)
		{
			int id = -1;
			Towns.c.load(Towns.f);
			if(id > 0 && Towns.c.getInt(id + ".resources") > 0)
			{
				p.getInventory().addItem(new ItemStack(Material.GOLD_BLOCK));
				p.sendMessage("Here is a resource!");
				Towns.c.set(id + ".resources", Towns.c.getInt(id + ".resources") - 1);
				Towns.c.save(Towns.f);
			}
			else if(id > 0 && Towns.c.getInt(id + ".resources") == 0)
				p.sendMessage("No resouces left!");
			else
				p.sendMessage("You want a resouce, but you aren't in a town!");

		}
	} */

	public String findTownOrPlot(Location loc)
	{
		container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		regions = container.get(BukkitAdapter.adapt(Bukkit.getServer().getWorld("world")));
		BlockVector3 vec = BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
		ApplicableRegionSet rgs = regions.getApplicableRegions(vec);
		if(rgs.size() != 0)
			return rgs.getRegions().iterator().next().getId();
		return null;
	}

	public String getPlayerPlot(Player p)
	{
		File f = new File("plugins/SimpleData/players/" + p.getUniqueId() + ".yml");
		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		return c.getString("town.plot");
	}

	public void addMember(Player p, ProtectedRegion region) throws IOException, SQLException
	{
		DefaultDomain dd = region.getMembers();
		dd.addPlayer(WorldGuardPlugin.inst().wrapPlayer(p));
		File f = new File("plugins/SimpleData/players/" + p.getUniqueId() + ".yml");
		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		String town = c.getString("town.id");
		String newTown = region.getId().substring(region.getId().indexOf("_") + 1, region.getId().indexOf("_plot_"));
		if(town == null || town == "" || !town.equalsIgnoreCase(newTown))
		{
			c.set("town.id", newTown);
			if(town != null || town != "") //if part of another town before
				plugin.getDBManager().addMember(town, -1);
		}
		if(town == null || !town.equalsIgnoreCase(newTown))
			plugin.getDBManager().addMember(newTown, 1);
		c.set("town.plot", region.getId());
		c.save(f);
	}
	
	public void addTownMember(Player p, String town) throws IOException, SQLException
	{
		ProtectedRegion region = getRegion(region_prefix.concat(town).toLowerCase());
		DefaultDomain dd = region.getMembers();
		dd.addPlayer(WorldGuardPlugin.inst().wrapPlayer(p));
		File f = new File("plugins/SimpleData/players/" + p.getUniqueId() + ".yml");
		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		String townInFile = c.getString("town.id");
		if(townInFile != null && townInFile != "") //if was part of another town before
			plugin.getDBManager().addMember(townInFile, -1);
		
		plugin.getDBManager().addMember(townInFile, 1);
		c.set("town.id", town);
		c.save(f);
	}

	public void removeMember(Player p, ProtectedRegion region) throws IOException
	{
		DefaultDomain dd = region.getMembers();
		dd.removePlayer(WorldGuardPlugin.inst().wrapPlayer(p));
		File f = new File("plugins/SimpleData/players/" + p.getUniqueId() + ".yml");
		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		c.set("town.id", "");
		c.set("town.plot", "");
		c.save(f);
	}
}
