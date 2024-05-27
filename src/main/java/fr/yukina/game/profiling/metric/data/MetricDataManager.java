package fr.yukina.game.profiling.metric.data;

import lombok.Getter;

import java.util.Stack;

@Getter
public class MetricDataManager<V extends Number>
{
	private final String                  name;
	private final IMetricDataGenerator<V> dataGenerator;

	private double                       startRecordTime;
	private double                       endRecordTime;
	private double                       totalValue;
	private double                       totalTime;
	private double                       averageValue;
	private Stack<MetricDataSnapshot<V>> metricDataSnapshotMap;

	public MetricDataManager(final String nameIn, final IMetricDataGenerator<V> dataGeneratorIn)
	{
		this.name                  = nameIn;
		this.dataGenerator         = dataGeneratorIn;
		this.startRecordTime       = 0;
		this.endRecordTime         = 0;
		this.totalValue            = 0;
		this.totalTime             = 0;
		this.averageValue          = 0;
		this.metricDataSnapshotMap = new Stack<>();
	}

	public void start()
	{
		this.startRecordTime = System.nanoTime();
		MetricDataSnapshot<V> metricData;
		if (this.metricDataSnapshotMap.isEmpty())
		{
			metricData = new MetricDataSnapshot<>(this.name, this.dataGenerator);
			this.metricDataSnapshotMap.push(metricData);
		}
		else
		{
			metricData = this.metricDataSnapshotMap.peek();
		}
		metricData.start();
	}

	public void end()
	{
		this.endRecordTime = System.nanoTime();

		var metricData = this.metricDataSnapshotMap.peek();
		metricData.end();
		if (metricData.hasChanged())
		{
			this.metricDataSnapshotMap.push(new MetricDataSnapshot<>(this.name, this.dataGenerator));
		}

		if (metricData.value() != null)
		{
			this.totalValue += metricData.value().doubleValue();
		}
		this.totalTime += (this.endRecordTime - this.startRecordTime);
		this.averageValue = this.totalValue / this.totalTime;
	}

	public void reset()
	{
		this.startRecordTime = 0;
		this.endRecordTime   = 0;
		this.totalValue      = 0;
		this.totalTime       = 0;
		this.averageValue    = 0;
		this.metricDataSnapshotMap.clear();
	}
}