package com.siralpega.Kingdoms.Towns;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.siralpega.Kingdoms.Kingdoms;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;

import net.md_5.bungee.api.ChatColor;

public class TownBuilder 
{
	private final Kingdoms plugin;
	private static ItemStack townPlacerItem;
	private final int basicSize = 60;
	public TownBuilder(Kingdoms instance) 
	{
		plugin = instance;	
		townPlacerItem = new ItemStack(Material.CORNFLOWER);
		ItemMeta im = townPlacerItem.getItemMeta();
		im.setDisplayName("Town Placer");
		List<String> lore;
		if(!im.hasLore())
			lore = new ArrayList<String>();
		else
			lore = im.getLore();
		lore.add("Left-click to place town");
		im.setLore(lore);
		townPlacerItem.setItemMeta(im);
	}

	public static ItemStack getTownBuilderItem()
	{
		return townPlacerItem;
	}

	public void build(Player p, Location loc, String name, String team, String direction) throws IncompleteRegionException, SQLException, CircularInheritanceException, IOException
	{
		Location second = new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
		if(direction.equalsIgnoreCase("west"))
			second = second.add(-basicSize, 0, basicSize);
		else if(direction.equalsIgnoreCase("south"))
			second = second.add(basicSize, 0, basicSize);
		else if(direction.equalsIgnoreCase("east"))
			second = second.add(basicSize, 0, basicSize);
		else
			second = second.add(basicSize, 0, -basicSize);

		//Sign Input: Line 1  == name, Line 2 == team, Line 3 == direction
		if(plugin.getRegionManager().addRegionRedux(p, loc, second, "town", name, direction))
		{
			plugin.getDBManager().addTown(name.toLowerCase(), team.toLowerCase(), KingdomsRegionManager.region_prefix.concat(name).toLowerCase());
			p.sendMessage(plugin.prefix + ChatColor.GREEN + "Town " + ChatColor.AQUA + name + ChatColor.GREEN + " created!");
			plugin.getLogger().info("Town " + name + " was added to the " + team + " team by " + p.getName()); 
			
			plugin.getRegionManager().addTownMember(p, name); 
			
			p.getInventory().remove(townPlacerItem);
			Material mat = Material.getMaterial(team.toUpperCase() + "_TERRACOTTA");
			if(mat == null)
				mat = Material.GREEN_TERRACOTTA;
			Bukkit.getWorld("world").getBlockAt(second).setType(mat);
		}
	}
}
