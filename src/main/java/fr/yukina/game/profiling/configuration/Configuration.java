package fr.yukina.game.profiling.configuration;

import lombok.Getter;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
public class Configuration
{
	private final Set<String> metrics;

	public Configuration(String... metricNamesIn)
	{
		this.metrics = new HashSet<>();

		if (metricNamesIn == null)
		{
			return;
		}

		for (String metricName : metricNamesIn)
		{
			if (metricName == null)
			{
				continue;
			}

			this.metrics.add(metricName);
		}
	}

	public final void addAll(String... metricNamesIn)
	{
		Collections.addAll(this.metrics, metricNamesIn);
	}

	public final void add(String metricIn)
	{
		this.metrics.add(metricIn);
	}

	public final void remove(String metricIn)
	{
		this.metrics.remove(metricIn);
	}
}
