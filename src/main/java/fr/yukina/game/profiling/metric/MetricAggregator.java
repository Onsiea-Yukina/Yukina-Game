package fr.yukina.game.profiling.metric;

import fr.yukina.game.profiling.metric.data.MetricDataSnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricAggregator
{
	public static Map<String, Double> aggregateMetrics(List<MetricDataSnapshot<?>> snapshots)
	{
		Map<String, Double> aggregatedData = new HashMap<>();
		for (MetricDataSnapshot<?> snapshot : snapshots)
		{
			aggregatedData.merge(snapshot.name(), snapshot.value().doubleValue(), Double::sum);
		}
		return aggregatedData;
	}
}