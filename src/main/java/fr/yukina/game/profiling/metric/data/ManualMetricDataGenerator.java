package fr.yukina.game.profiling.metric.data;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManualMetricDataGenerator<V extends Number> implements IMetricDataGenerator<V>
{
	private V value;

	@Override
	public V value(final MetricDataSnapshot<V> snapshotIn)
	{
		return value;
	}
}