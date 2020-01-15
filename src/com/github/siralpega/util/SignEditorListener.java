package com.github.siralpega.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import com.github.siralpega.Kingdoms.Kingdoms;
import com.github.siralpega.Kingdoms.Towns.StructureManager;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldguard.protection.regions.ProtectedRegion.CircularInheritanceException;


public class SignEditorListener implements Listener
{
	private UUID id;
	public SignEditorListener(Player p, Kingdoms inst)
	{
		id = p.getUniqueId();
		Bukkit.getServer().getPluginManager().registerEvents(this, inst);
	}
	@EventHandler
	public void onSignChange(SignChangeEvent event)
	{
		if(event.getPlayer().getUniqueId() != id)
			return;
		String[] s = event.getLines();
		org.bukkit.event.HandlerList.unregisterAll(this);
		try 
		{
			//Sign Input: Line 1  == name, Line 2 == team, Line 3 == direction
			//TODO: find a way to get the text on sign gui
			StructureManager.getTownBuilder().build(event.getPlayer(), event.getBlock().getLocation(), s[0], s[1], s[2]);
			event.getBlock().breakNaturally();
			Material mat = Material.getMaterial(s[1].toUpperCase() + "_TERRACOTTA");
			if(mat == null)
				mat = Material.GREEN_TERRACOTTA;
			event.getBlock().setType(mat);
		} 
		catch (IncompleteRegionException | SQLException | CircularInheritanceException | IOException e)
		{
			event.getPlayer().sendMessage(("Fatal error occured. Tell an admin"));
			e.printStackTrace();
			event.setCancelled(true);
		}
	}
}
