package com.github.siralpega.Kingdoms.Towns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.github.siralpega.Kingdoms.Kingdoms;

public class PlotBuilder
{
	@SuppressWarnings("unused")
	private final Kingdoms plugin;
	private final int MINIMUM_PLOT_SIZE = 5;
	
	private static ItemStack plotItem;
	private Map<UUID, Location> plotLocs;
	public PlotBuilder(Kingdoms instance) 
	{
		plugin = instance;	
		plotLocs = new HashMap<UUID, Location>();
		plotItem = new ItemStack(Material.FLINT);
		ItemMeta im = plotItem.getItemMeta();
		im.setDisplayName("Plot Placer");
		List<String> lore;
		if(!im.hasLore())
			lore = new ArrayList<String>();
		else
			lore = im.getLore();
		lore.add("Left-click to select a position for a plot");
		im.setLore(lore);
		plotItem.setItemMeta(im);
	}

	public static ItemStack getPlotBuilderItem()
	{
		if(plotItem == null)
		{
			plotItem = new ItemStack(Material.FLINT);
			ItemMeta im = plotItem.getItemMeta();
			im.setDisplayName("Plot Placer");
			List<String> lore;
			if(!im.hasLore())
				lore = new ArrayList<String>();
			else
				lore = im.getLore();
			lore.add("Left-click to select a position for a plot");
			im.setLore(lore);
			plotItem.setItemMeta(im);
		}
		return plotItem;
	}
	
	public Location playerAddPlot(UUID id, Location l)
	{
		if(plotLocs.containsKey(id))
		{
			Location f = plotLocs.get(id);
			buildPlot(f, l);
			plotLocs.remove(id);
			return f;
		}
		plotLocs.put(id, l);	
		return null;
	}
	
	public void generateUtilPlots(Location bound1, Location bound2)
	{
		
	}

	private void buildPlot(Location pt1, Location pt2)
	{
		//	Bukkit.broadcastMessage("(" + pt1.getBlockX() +  "," + pt1.getBlockY() +  "," + pt1.getBlockZ() +  ") -> (" + pt2.getBlockX() +  "," + pt2.getBlockY() +  "," + pt2.getBlockZ() +  ")");
		if(Math.abs(pt1.getBlockX() - pt2.getBlockX()) < MINIMUM_PLOT_SIZE || Math.abs(pt1.getBlockZ() - pt2.getBlockZ()) < MINIMUM_PLOT_SIZE)
			return;

		Random r = new Random();
		String terra;
		double num = r.nextDouble();
		if(num <= .25) terra = "RED";
		else if(num > .25 && num <= .5) terra = "BLUE";
		else if(num > .5 && num <= .75) terra = "PURPLE";
		else terra = "PINK";
		Material glass = Material.getMaterial(terra + "_TERRACOTTA");
		for(int i = pt1.getBlockX(); i <= pt2.getBlockX(); i++)
		{
			Bukkit.getWorld("world").getBlockAt(i, pt1.getBlockY(), pt1.getBlockZ()).setType(glass);
			Bukkit.getWorld("world").getBlockAt(i, pt1.getBlockY(), pt2.getBlockZ()).setType(glass);
		}
		for(int i = pt1.getBlockZ(); i <= pt2.getBlockZ(); i++)
		{
			Bukkit.getWorld("world").getBlockAt(pt1.getBlockX(), pt1.getBlockY(), i).setType(glass);
			Bukkit.getWorld("world").getBlockAt(pt2.getBlockX(), pt1.getBlockY(), i).setType(glass);
		}

	}
}
