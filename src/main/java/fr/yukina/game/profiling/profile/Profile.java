package fr.yukina.game.profiling.profile;

import fr.yukina.game.profiling.configuration.Configuration;
import fr.yukina.game.profiling.metric.MetricManager;
import fr.yukina.game.profiling.snapshot.ProfilerSnapshotManager;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Profile
{
	private @Setter Configuration           configuration;
	private final   String                  name;
	private final   MetricManager           metricFunctionManager;
	private final   ProfilerSnapshotManager snapshotManager;
	private         String                  thread;

	public Profile(String nameIn, MetricManager metricFunctionManagerIn)
	{
		this.name                  = nameIn;
		this.metricFunctionManager = metricFunctionManagerIn;
		this.snapshotManager       = new ProfilerSnapshotManager(this);
	}

	public final void start()
	{
		this.thread = Thread.currentThread().getName();
		this.snapshotManager.start();
	}

	public final void end()
	{
		this.snapshotManager.end();
	}

	public final void reset()
	{
		this.snapshotManager.reset();
	}
}