package com.github.siralpega.Kingdoms;
import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

public class CommandSkills implements CommandExecutor 
{
	private final Kingdoms plugin;
	public CommandSkills(Kingdoms instance) 
	{
		this.plugin = instance;	
	}
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		if(command.getLabel().equals("skills"))
		{
			String[] skills = getSkills(sender);
			String answer = "";
			for(int i = 0; i < skills.length; i++)
				if(skills[i] != null)
					answer = answer + skills[i] + " ";
			sender.sendMessage(ChatColor.GREEN + "Your Skill(s): " + ChatColor.AQUA + answer);  
		}
		else if(command.getLabel().equals("setskill") && sender.hasPermission("king.setskill"))
		{
			try
			{
				Player p = (Bukkit.getServer().getPlayer(args[0]));
				setPlayerSkill(p, Integer.parseInt(args[1]), args[2]);
				sender.sendMessage(plugin.prefix + p.getName() + " skill #" + args[1] + " is now " + ChatColor.AQUA + args[2]);
			}
			catch(Exception e)
			{
				sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error! / Something went wrong");
			}
		}
		else
			sender.sendMessage(plugin.prefix + ChatColor.RED + "Syntax Error!");
		return true;
	}
	
	public String[] getSkills(CommandSender sender)
	{
		if(sender instanceof Player)
		{
			Player p = (Player) sender;
			String[] re = new String[3];
			File f = new File("plugins/SimpleData/players/" + p.getUniqueId() + ".yml");
			FileConfiguration c = YamlConfiguration.loadConfiguration(f);
			re[0] = c.getString("skills.1");
			re[1] = c.getString("skills.2");
			re[2] = c.getString("skills.3");
			return re;
		}
		return null;
	}
	
	public static void setPlayerSkill(Player p, int num, String value) throws IOException
	{
		File f = new File("plugins/SimpleData/players/" + p.getUniqueId() + ".yml"); //YOU CAN USE SIMPLE DATA FOR SKILLS!!!!!
		FileConfiguration c = YamlConfiguration.loadConfiguration(f);
		c.set("skills." + num, value);
		c.save(f);
	}
}
