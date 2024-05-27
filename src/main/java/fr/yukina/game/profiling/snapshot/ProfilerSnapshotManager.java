package fr.yukina.game.profiling.snapshot;

import fr.yukina.game.profiling.metric.data.MetricDataManager;
import fr.yukina.game.profiling.profile.Profile;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

@Getter
public class ProfilerSnapshotManager
{
	private final Profile                           profile;
	private       long                              start;
	private       long                              end;
	private       long                              callCount;
	private final Map<String, MetricDataManager<?>> metricDataManagerMap;

	public ProfilerSnapshotManager(Profile profileIn)
	{
		this.profile              = profileIn;
		this.metricDataManagerMap = new HashMap<>();
	}

	public final void start()
	{
		if (this.profile.configuration() != null)
		{
			var toRemoves = new Stack<String>();
			for (var metricName : this.metricDataManagerMap.keySet())
			{
				if (!this.profile.configuration().metrics().contains(metricName))
				{
					toRemoves.add(metricName);
				}
			}

			while (!toRemoves.isEmpty())
			{
				this.metricDataManagerMap.remove(toRemoves.pop());
			}

			for (var metricName : this.profile.configuration().metrics())
			{
				this.metricDataManagerMap.put(metricName, new MetricDataManager(metricName,
				                                                                this.profile.metricFunctionManager()
				                                                                            .get(metricName)));
			}
		}

		this.callCount++;
		this.start = System.nanoTime();
		for (var metricDataManager : this.metricDataManagerMap.values())
		{
			metricDataManager.start();
		}
	}

	public final void end()
	{
		for (var metricDataManager : this.metricDataManagerMap.values())
		{
			metricDataManager.end();
		}
		this.end = System.nanoTime();
	}

	public final void reset()
	{
		for (var metricDataManager : this.metricDataManagerMap.values())
		{
			metricDataManager.reset();
		}
		this.callCount = 0;
		this.start     = 0L;
		this.end       = 0L;
	}
}