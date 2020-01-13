package com.siralpega.Kingdoms;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class Skills 
{
	private final Kingdoms plugin;
	static File f;
	static FileConfiguration c;
	public Skills(Kingdoms instance) 
	{
		this.plugin = instance;		
		f = new File(plugin.getDataFolder() + File.separator + "skills.yml");
		c = YamlConfiguration.loadConfiguration(f);
	}

	
	public int getSkillMod(String skill)
	{
		if(!c.contains(skill.toLowerCase()))
			return -1;
		return 0;
	}
	
	public void setSkillMod(String skill)
	{
		
	}
}
