package com.github.siralpega.Kingdoms;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/* TaintSpread
 * Manages the spread of the "Tainted Land" in the world.
 * 
 * 
 */
public class TaintSpread 
{
	private final Kingdoms plugin;
	private Location[] spreadPoints;
	File f;
	FileConfiguration c;

	public TaintSpread(Kingdoms instance) 
	{
		this.plugin = instance;		
		spreadPoints = new Location[5];
		f = new File(plugin.getDataFolder() + File.separator + "points.yml");
		c = YamlConfiguration.loadConfiguration(f);

		for(int i = 0; i < spreadPoints.length; i++)
			spreadPoints[i] = getSpreadLocation(i);
	}

	public void spread() throws IOException
	{
		Random r = new Random(10);
		int chance;
		for(int i = 0; i < spreadPoints.length; i++)
		{
			chance = r.nextInt();
			if(chance % 5 == 0)
			{
				spreadPoints[i].add(r.nextInt(), r.nextInt(), r.nextInt());
				Block b = spreadPoints[i].getWorld().getBlockAt(spreadPoints[i]);
				b.setType(Material.MYCELIUM);
				
				
			}
		}
		updateLocations();
	}

	public void updateLocations() throws IOException
	{
		for(int i = 0; i < spreadPoints.length; i++)
		{
			c.set(i + ".x", spreadPoints[i].getBlockX());
			c.set(i + ".y", spreadPoints[i].getBlockY());
			c.set(i + ".z", spreadPoints[i].getBlockZ());
			c.save(f);
		}
	}

	public Location getSpreadLocation(int num)
	{
		return new Location(Bukkit.getServer().getWorld("world"), c.getInt(num + ".x"), c.getInt(num + ".y"), c.getInt(num + ".z"));
	}
}
