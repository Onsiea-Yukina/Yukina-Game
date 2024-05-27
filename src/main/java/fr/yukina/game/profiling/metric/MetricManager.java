package fr.yukina.game.profiling.metric;

import fr.yukina.game.profiling.metric.data.IMetricDataGenerator;
import fr.yukina.game.profiling.metric.data.ManualMetricDataGenerator;
import fr.yukina.game.profiling.metric.data.SuppliedMetricDataGenerator;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Getter
public class MetricManager
{
	private final Map<String, MetricInfo>                             metricInfoMap;
	private final Map<String, IMetricDataGenerator<? extends Number>> metricFunctionMap;

	public MetricManager()
	{
		this.metricInfoMap     = new HashMap<>();
		this.metricFunctionMap = new HashMap<>();
	}

	/**
	 * METRIC INFO
	 */

	/**
	 * @param nameIn
	 * @param metricInfoIn
	 */
	public final void addMetricInfo(String nameIn, MetricInfo metricInfoIn)
	{
		this.metricInfoMap.put(nameIn, metricInfoIn);
	}

	/**
	 * @param nameIn
	 * @return
	 */
	public final MetricInfo getMetricInfo(String nameIn)
	{
		return this.metricInfoMap.get(nameIn);
	}

	/**
	 * @param nameIn
	 */
	public final void removeMetricInfo(String nameIn)
	{
		this.metricInfoMap.remove(nameIn);
	}

	/**
	 * FUNCTIONS
	 */

	public final <V extends Number> void add(String nameIn, IMetricDataGenerator<V> functionIn)
	{
		this.metricFunctionMap.put(nameIn, functionIn);
	}

	/**
	 * @param nameIn
	 * @param functionIn
	 * @param <V>
	 */
	public final <V extends Number> void add(String nameIn, Supplier<V> functionIn)
	{
		this.metricFunctionMap.put(nameIn, new SuppliedMetricDataGenerator<V>(functionIn));
	}

	/**
	 * @param nameIn
	 * @param typeIn
	 * @param <V>
	 */
	public final <V extends Number> void add(String nameIn, Class<V> typeIn)
	{
		this.metricFunctionMap.put(nameIn, new ManualMetricDataGenerator<V>());
	}

	/**
	 * @param nameIn
	 * @return
	 */
	public final IMetricDataGenerator<?> get(String nameIn)
	{
		return this.metricFunctionMap.get(nameIn);
	}

	/**
	 * @param nameIn
	 */
	public final void remove(String nameIn)
	{
		this.metricFunctionMap.remove(nameIn);
	}

	public final void clear()
	{
		this.metricFunctionMap.clear();
	}
}