package fr.yukina.game.profiling.profile;

import fr.yukina.game.profiling.configuration.Configuration;
import fr.yukina.game.profiling.metric.MetricManager;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Getter
public class ProfileManager
{
	private final   MetricManager        metricFunctionManager;
	private final   Map<String, Profile> profileMap;
	private final   Stack<Profile>       startedProfileStack = new Stack<>();
	private @Setter Configuration        basicConfiguration;

	public ProfileManager(final MetricManager metricFunctionManagerIn)
	{
		this.metricFunctionManager = metricFunctionManagerIn;
		this.profileMap            = new HashMap<>();
		this.basicConfiguration    = new Configuration();
	}

	public Profile get(String nameIn, String... metricNamesIn)
	{
		var configuration = this.basicConfiguration;
		if (metricNamesIn != null && metricNamesIn.length > 0)
		{
			configuration = new Configuration(metricNamesIn);
		}
		System.out.println("> " + configuration.metrics());
		var profile = this.profileMap.computeIfAbsent(nameIn, k -> new Profile(k, this.metricFunctionManager));
		profile.configuration(configuration);

		return profile;
	}

	public void start(String nameIn)
	{
		var profile = this.get(nameIn);
		this.startedProfileStack.push(profile);
		profile.start();
	}

	public void end(String nameIn)
	{
		this.get(nameIn).end();
	}

	public void end()
	{
		while (!this.startedProfileStack.isEmpty())
		{
			this.startedProfileStack.pop().end();
		}
	}

	public void reset(String nameIn)
	{
		this.get(nameIn).reset();
	}

	public void resetAll()
	{
		this.profileMap.values().forEach(Profile::reset);
	}

	public void clear()
	{
		this.profileMap.clear();
	}
}