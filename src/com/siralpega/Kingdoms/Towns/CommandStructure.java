package com.siralpega.Kingdoms.Towns;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.siralpega.Kingdoms.Kingdoms;
import com.sk89q.worldedit.WorldEditException;

import net.md_5.bungee.api.ChatColor;

public class CommandStructure implements CommandExecutor 
{

	private final Kingdoms plugin;
	public CommandStructure(Kingdoms instance) 
	{
		plugin = instance;	
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(command.getLabel().equals("struct"))
		{
			if(args.length == 1 && sender instanceof Player)
			{
				try 
				{
				//	Player p = (Player) sender;
				//	plugin.getStructureManager().build(args[0], p.getLocation());
					plugin.getStructureManager().upgradePlot(plugin.getRegionManager().getRegion(args[0]), "green");
				} 
				catch (IOException | WorldEditException e) 
				{
					sender.sendMessage(plugin.prefix + ChatColor.RED + "Error: Failed to build. Tell an admin.");

					e.printStackTrace();
				}
			}
			else
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! /struct <schematic>");
		}
		return true;
	}	
}
