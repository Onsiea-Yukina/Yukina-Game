package fr.yukina.game.profiling.configuration;

import java.util.HashMap;
import java.util.Map;

public class ConfigurationManager
{
	private final Map<String, Configuration> configurationMap;

	public ConfigurationManager()
	{
		this.configurationMap = new HashMap<>();
	}

	public final void add(String nameIn, String... metricNamesIn)
	{
		if (nameIn == null)
		{
			throw new IllegalArgumentException("[ERROR] ConfigurationManager : configuration name cannot be null");
		}

		this.configurationMap.put(nameIn, new Configuration(metricNamesIn));
	}

	public final void remove(String nameIn)
	{
		this.configurationMap.remove(nameIn);
	}

	public final Configuration get(String nameIn)
	{
		return this.configurationMap.get(nameIn);
	}

	public final void clear()
	{
		this.configurationMap.clear();
	}
}
