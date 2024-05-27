package fr.yukina.game.profiling.metric.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DurationMetricDataGenerator implements IMetricDataGenerator<Long>
{
	@Override
	public Long value(MetricDataSnapshot<Long> snapshotIn)
	{
		var time = snapshotIn.endRecordTime() - snapshotIn.startRecordTime();

		return time;
	}
}