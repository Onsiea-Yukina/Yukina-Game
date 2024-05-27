package fr.yukina.game.profiling.metric.data;

public interface IMetricDataGenerator<V extends Number>
{
	V value(MetricDataSnapshot<V> snapshotIn);
}