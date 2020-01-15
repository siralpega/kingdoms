package com.github.siralpega.Kingdoms.Towns;

import java.io.IOException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.github.siralpega.Kingdoms.Kingdoms;
import com.github.siralpega.Kingdoms.Towns.StructureBuild.Direction;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

import net.md_5.bungee.api.ChatColor;

public class StructureManager 
{
	private final Kingdoms plugin;
	private final StructureBuild builder;
	private static TownBuilder tb;
	public StructureManager(Kingdoms instance, StructureBuild builder) 
	{
		plugin = instance;	
		this.builder = builder;
		tb = new TownBuilder(instance);
	}

	public void build(String schem, Location loc, Direction dir) throws IOException, WorldEditException
	{
		builder.buildStucture(schem, loc, dir);
	}
	public void build(String schem, Location loc) throws IOException, WorldEditException
	{
		Direction dir = null;
		float yaw = loc.getYaw();
		if(yaw >= 45 && yaw < 135) //west
			dir = Direction.WEST;
		else if(yaw >= 135 && yaw < 225) //north
			dir = Direction.NORTH;
		else if(yaw >= 225 && yaw < 315) //east
			dir = Direction.EAST;
		else if(yaw >= 315 || yaw < 45) //south
			dir = Direction.SOUTH;
		build(schem, loc, dir);
	}

	public void upgradePlot(ProtectedRegion region, String team) throws IOException, WorldEditException
	{		
	 //Can make it more specific to an area.
		int currLevel = 0;
		String upgrade = getSchematicUpgrade(team, currLevel, region);
		List<BlockVector2> points = region.getPoints();
		String flagDir = region.getFlag(Kingdoms.DIRECTION_FLAG);
		if(flagDir == null)
			flagDir = "north";
		Direction dir = Direction.NORTH;
		Location origin = null;
		int y = 0;
		if(region.getMaximumPoint().getBlockY() - region.getMinimumPoint().getBlockY() == 60)
			y = 20;
		
		//Clear the region first
		builder.clearRegion(region, y);
		
		if(flagDir.equalsIgnoreCase("west"))
		{
			dir = Direction.WEST;
			origin = new Location(Bukkit.getWorld("world"), points.get(2).getBlockX(), region.getMinimumPoint().getBlockY() + y, points.get(2).getBlockZ());
		}
		else if(flagDir.equalsIgnoreCase("south"))
		{
			dir = Direction.SOUTH;
			origin = new Location(Bukkit.getWorld("world"), points.get(1).getBlockX(), region.getMinimumPoint().getBlockY() + y, points.get(1).getBlockZ());
		}
		else if(flagDir.equalsIgnoreCase("east"))
		{
			dir = Direction.EAST;
			origin = new Location(Bukkit.getWorld("world"), points.get(0).getBlockX(), region.getMinimumPoint().getBlockY() + y, points.get(0).getBlockZ());
		}
		else
			origin = new Location(Bukkit.getWorld("world"), points.get(3).getBlockX(), region.getMinimumPoint().getBlockY() + y, points.get(3).getBlockZ());
		Bukkit.broadcast(plugin.prefix + "Upgrading " + ChatColor.AQUA + region.getId() + ChatColor.GREEN + " to " + ChatColor.AQUA + upgrade, "king.admin");
		build(upgrade, origin, dir);
	}

	public String getSchematic(String team, int level)
	{
		//based on team (or area if more specific) and the level of a plot, return the name of the schematic
		return "demo.schematic";
	}

	public String getSchematicUpgrade(String team, int currLevel, ProtectedRegion rg)
	{
		//based on team (or area if more specific) and the level of a plot, return the name of the upgraded schematic
		int x = Math.abs(rg.getMaximumPoint().getBlockX() - rg.getMinimumPoint().getBlockX());
		int z = Math.abs(rg.getMaximumPoint().getBlockZ() - rg.getMinimumPoint().getBlockZ());
		return team + "_" + currLevel + "_" + x + "x" + z + ".schem"; //house.schem
	}
	
	public static TownBuilder getTownBuilder()
	{
		return tb;
	}

}
