package fr.yukina.game.profiling.metric.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * - Metric Data is a snapshot of a recording.<br>
 * - The data is identifiable by its name.<br>
 * - We can retrieve the current value and the previous one.<br>
 * - endTime-startTime provided by the "duration" method is the length of time the value has not changed.<br>
 * - The data is generated using a generator (IMetricDataGenerator<V>), it can be manual, supplied or any other.<br>
 * <br>
 * @param <V>
 * @see IMetricDataGenerator<V>
 * @see ManualMetricDataGenerator
 * @see SuppliedMetricDataGenerator
 */
@Getter
@RequiredArgsConstructor
public class MetricDataSnapshot<V extends Number>
{
	private final String name;

	private long startRecordTime;   // nanoseconds
	private long endRecordTime;     // nanoseconds

	private long startTime = -1;         // nanoseconds
	private long endTime;           // nanoseconds

	private V lastValue;
	private V value;

	private final IMetricDataGenerator<V> dataGenerator;

	public final long duration()
	{
		System.out.println(this.endTime + " - " + this.startTime + " = " + (this.endTime - this.startTime));
		return this.endTime - this.startTime;
	}

	public final boolean hasChanged()
	{
		return this.lastValue != this.value;
	}

	public final void start()
	{
		if (this.startTime < 0)
		{
			this.startTime = System.nanoTime();
			this.endTime   = this.startTime;
		}
		this.startRecordTime = System.nanoTime();
	}

	public final void end()
	{
		this.endRecordTime = System.nanoTime();
		this.lastValue     = this.value;
		this.value         = this.dataGenerator.value(this);
		if (!hasChanged())
		{
			this.endTime = System.nanoTime();
		}
	}

	public final void reset()
	{
		this.startRecordTime = 0L;
		this.endRecordTime   = 0L;
		this.value           = null;
		this.lastValue       = null;
		this.startTime       = 0L;
		this.endTime         = 0L;
	}

	public final MetricDataSnapshot<V> copy()
	{
		var metricData = new MetricDataSnapshot(this.name, this.dataGenerator);
		metricData.lastValue       = this.lastValue;
		metricData.value           = this.value;
		metricData.startTime       = this.startTime;
		metricData.endTime         = this.endTime;
		metricData.startRecordTime = this.startRecordTime;
		metricData.endRecordTime   = this.endRecordTime;

		return metricData;
	}
}