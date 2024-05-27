package fr.yukina.game.profiling.metric.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
@AllArgsConstructor
public class SuppliedMetricDataGenerator<V extends Number> implements IMetricDataGenerator<V>
{
	private final Supplier<V> valueSupplier;

	@Override
	public V value(MetricDataSnapshot<V> snapshotIn)
	{
		return this.valueSupplier.get();
	}
}