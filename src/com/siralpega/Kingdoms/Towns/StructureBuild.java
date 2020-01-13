package com.siralpega.Kingdoms.Towns;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.siralpega.Kingdoms.Kingdoms;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class StructureBuild 
{
	private final Kingdoms plugin;
	public static enum Direction {NORTH, SOUTH, EAST, WEST};

	public StructureBuild(Kingdoms instance) 
	{
		plugin = instance;	
	}

	public void buildStucture(String schem, Location loc, Direction dir) throws IOException, WorldEditException
	{
		//load schematic
		File f = new File(plugin.getDataFolder() + "/schematics/" + schem);
		if(!f.exists())
		{
			plugin.getLogger().warning("Can't build strucutre because can't find schematic: " + schem + "!");
			return;
		}
		ClipboardFormat format = ClipboardFormats.findByFile(f);
		Clipboard clipboard;

		try (ClipboardReader reader = format.getReader(new FileInputStream(f))) {
			clipboard = reader.read();
		}
		ClipboardHolder ch = new ClipboardHolder(clipboard);
		//Rotation
		double rot = 0;
		if(dir == Direction.WEST)
		{
			loc = loc.add(1, 1, 0);
			rot = 90;
		}
		else if(dir == Direction.SOUTH)
		{
			loc = loc.add(0, 1, -1);
			rot = 180;
		}
		else if(dir == Direction.EAST)
		{
			loc = loc.add(-1, 1, 0);
			rot = 270;
		}
		else
			loc = loc.add(0, 1, 1);
		AffineTransform trans = new AffineTransform().rotateY(rot);
		ch.setTransform(ch.getTransform().combine(trans));
		try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
				.getEditSession(BukkitAdapter.adapt(loc.getWorld()), -1)) {
			Operation operation = ch.createPaste(editSession)
					.to(BlockVector3.at(loc.getX(), loc.getY(), loc.getZ())).ignoreAirBlocks(false).build();
			Operations.complete(operation);
		}
	}	

	public void clearRegion(ProtectedRegion region, int y)
	{
		BlockVector3 min = region.getMinimumPoint().add(0, 20, 0);
		BlockVector3 max = region.getMaximumPoint();
		if(min.getBlockX() - max.getBlockX() > 0)
		{
			int minx = min.getBlockX();
			min = min.add(max.getBlockX() - min.getBlockX(), 0, 0);
			max = max.add(minx - max.getBlockX(), 0, 0);
		}
		if(min.getBlockZ() - max.getBlockZ() > 0)
		{

			int minz = min.getBlockZ();
			min = min.add(0, 0, max.getBlockZ() - min.getBlockZ());
			max = max.add(0, 0, minz - max.getBlockZ());
		}
		if(min.getBlockY() - max.getBlockY() > 0)
		{

			int miny = min.getBlockY();
			min = min.add(0, max.getBlockY() - min.getBlockY(), 0);
			max = max.add(0, miny - max.getBlockY(), 0);
		}
		Location bLoc = new Location(Bukkit.getWorld("world"), min.getBlockX(), min.getBlockY(), min.getBlockZ());
		Block b;
		World w = Bukkit.getWorld("world");
		Material mat;
		if(Math.random() < .5)
			 mat = Material.GREEN_TERRACOTTA;
		else
			 mat = Material.LIME_TERRACOTTA;
		for(bLoc.getBlockY(); bLoc.getBlockY() <= max.getBlockY(); bLoc.setY(bLoc.getBlockY() + 1))
		{
			bLoc.setX(min.getBlockX());
			for(bLoc.getBlockX(); bLoc.getBlockX() <= max.getBlockX(); bLoc.setX(bLoc.getBlockX() + 1))
			{
				bLoc.setZ(min.getBlockZ());
				for(bLoc.getBlockZ(); bLoc.getBlockZ() <= max.getBlockZ(); bLoc.setZ(bLoc.getBlockZ() + 1))
				{
					b = w.getBlockAt(bLoc);
					if(bLoc.getBlockY() == min.getBlockY())
						b.setType(mat);
					else
						b.setType(Material.AIR);
				}
			}
		}
	}
}
