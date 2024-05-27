package fr.yukina.game.profiling;

import fr.yukina.game.profiling.configuration.Configuration;
import fr.yukina.game.profiling.configuration.ConfigurationManager;
import fr.yukina.game.profiling.index.ProfilesKeyManager;
import fr.yukina.game.profiling.metric.MetricInfo;
import fr.yukina.game.profiling.metric.MetricManager;
import fr.yukina.game.profiling.metric.data.IMetricDataGenerator;
import fr.yukina.game.profiling.profile.ProfileManager;
import fr.yukina.game.profiling.writer.ProfilerWriter;
import fr.yukina.game.profiling.writer.ProfilerWriterManager;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class Profiler
{
	private final ConfigurationManager  configurationManager;
	private final MetricManager         metricManager;
	private final ProfilesKeyManager    profilesKeyManager;
	private final ProfileManager        profileManager;
	private final ProfilerWriterManager writerManager;

	public Profiler()
	{
		this.configurationManager = new ConfigurationManager();
		this.metricManager        = new MetricManager();
		this.profilesKeyManager   = new ProfilesKeyManager();
		this.profileManager       = new ProfileManager(this.metricManager);
		this.writerManager        = new ProfilerWriterManager();
	}

	/**
	 * CONFIGURATION
	 */

	public void updateConfiguration(String nameIn, String... metricNamesIn)
	{
		Configuration config = this.configurationManager.get(nameIn);
		if (config != null)
		{
			for (String metricName : metricNamesIn)
			{
				config.add(metricName);
			}
		}
		else
		{
			this.configurationManager.add(nameIn, metricNamesIn);
		}
	}

	/**
	 * @param nameIn
	 * @param metricNamesIn
	 */
	public void addConfiguration(String nameIn, String... metricNamesIn)
	{
		this.configurationManager.add(nameIn, metricNamesIn);
	}

	/**
	 * @param nameIn
	 */
	public void removeConfiguration(String nameIn)
	{
		this.configurationManager.remove(nameIn);
	}

	/**
	 * METRIC INFO
	 */

	/**
	 * @param nameIn
	 * @param descriptionIn
	 * @param unitIn
	 */
	public void addMetricInfo(String nameIn, String descriptionIn, String unitIn)
	{
		this.metricManager.addMetricInfo(nameIn, new MetricInfo(nameIn, descriptionIn, unitIn));
	}

	/**
	 * @param nameIn
	 */
	public void removeMetricInfo(String nameIn)
	{
		this.metricManager.removeMetricInfo(nameIn);
	}

	/**
	 * METRIC FUNCTION
	 */

	/**
	 * @param nameIn
	 * @param functionIn
	 */
	public <V extends Number> void addMetricFunction(String nameIn, IMetricDataGenerator<V> functionIn)
	{
		this.metricManager.add(nameIn, functionIn);
	}

	/**
	 * @param nameIn
	 * @param functionIn
	 */
	public <V extends Number> void addMetricFunction(String nameIn, Supplier<V> functionIn)
	{
		this.metricManager.add(nameIn, functionIn);
	}

	/**
	 * @param nameIn
	 * @param typeIn
	 */
	public <V extends Number> void addMetricFunction(String nameIn, Class<V> typeIn)
	{
		this.metricManager.add(nameIn, typeIn);
	}

	/**
	 * @param nameIn
	 */
	public void removeMetricFunction(String nameIn)
	{
		this.metricManager.remove(nameIn);
	}

	/**
	 * PROFILE KEY
	 */

	/**
	 * @param nameIn
	 */
	public String getProfileKey(boolean isIndexedIn, String nameIn)
	{
		return this.profilesKeyManager.key(isIndexedIn, nameIn);
	}

	/**
	 * @param nameIn
	 */
	public void removeProfileKey(String nameIn)
	{
		this.profilesKeyManager.remove(nameIn);
	}

	/**
	 * PROFILE
	 */

	/**
	 * @param nameIn
	 */
	public void start(String nameIn)
	{
		this.profilesKeyManager.key(true, nameIn);
		this.profileManager.start(nameIn);
	}

	/**
	 * @param nameIn
	 */
	public void end(String nameIn)
	{
		this.profileManager.end(nameIn);
		this.writerManager.write();
	}

	public void end()
	{
		this.profileManager.end();
		this.writerManager.write();
	}

	/**
	 * @param nameIn
	 */
	public void reset(String nameIn)
	{
		this.profileManager.reset(nameIn);
	}

	public void resetAll()
	{
		this.profileManager.resetAll();
	}

	public void clearProfilerManager()
	{
		this.profileManager.clear();
	}

	/**
	 * WRITER
	 */
	public void write()
	{

	}

	/**
	 * @param nameIn
	 */
	public void addWriter(String nameIn, ProfilerWriter writerIn)
	{
		this.writerManager.add(nameIn, writerIn);
	}

	/**
	 * @param nameIn
	 */
	public void removeWriter(String nameIn)
	{
		this.writerManager.remove(nameIn);
	}

	public void clearWriter()
	{
		this.writerManager.clear();
	}

	/**
	 * GLOBAL
	 */

	public final void clear()
	{
		this.configurationManager.clear();
		this.metricManager.clear();
		this.profilesKeyManager.clear();
		this.profileManager.clear();
		this.writerManager.clear();
	}
}