package fr.yukina.game.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Statistic
{
	private       int          iteration;
	private       double       current;
	private       double       previous;
	private       double       low;
	private       double       high;
	private       double       average;
	private       double       standardDeviation;
	private       double       variance;
	private       double       variation;
	private       double       range;
	private       double       median;
	private       double       iqr;
	private       double       q1;
	private       double       q3;
	private       double       total;
	private final List<Double> histogram;
	private       int          histogramSize;
	private       boolean      hasChanged;

	public Statistic(int histogramSize)
	{
		this.current           = 0.0D;
		this.low               = Double.POSITIVE_INFINITY;
		this.high              = Double.NEGATIVE_INFINITY;
		this.average           = 0.0D;
		this.standardDeviation = 0.0D;
		this.variance          = 0.0D;
		this.total             = 0.0D;
		this.histogram         = new ArrayList<>();
		this.histogramSize     = histogramSize;
		this.hasChanged        = false;
	}

	public final void set(double valueIn)
	{
		this.total += valueIn;
		if (valueIn < this.low)
		{
			this.low = valueIn;
		}
		if (valueIn > this.high)
		{
			this.high = valueIn;
		}
		this.range = this.high - this.low;
		this.average += valueIn;
		this.variance += (valueIn - this.average) * (valueIn - this.average);
		if (this.iteration > 0)
		{
			this.average /= 2.0D;
			this.variance /= 2.0D;
		}
		this.standardDeviation = Math.sqrt(this.variance);

		if (this.histogram.size() + 1 > this.histogramSize)
		{
			this.histogram.remove(0);
		}
		this.histogram.add(valueIn);
		this.median = this.histogram.get((this.histogram.size() - 1) / 2);
		this.iqr    = this.histogram.size() - 1 / 4 - this.histogram.size() - 1 * 3 / 4;
		this.q1     = this.histogram.get((this.histogram.size() - 1) / 4);
		this.q3     = this.histogram.get((this.histogram.size() - 1) * 3 / 4);

		if (this.current != valueIn)
		{
			this.hasChanged = true;
		}
		this.variation = valueIn - this.previous;
		this.previous  = this.current;
		this.current   = valueIn;
		this.iteration++;
	}

	public final void clear()
	{
		this.histogram.clear();
	}

	public final void reset()
	{
		this.iteration         = 0;
		this.current           = 0.0D;
		this.low               = 0.0D;
		this.high              = 0.0D;
		this.average           = 0.0D;
		this.standardDeviation = 0.0D;
		this.variance          = 0.0D;
		this.total             = 0.0D;
		this.histogram.clear();
		this.hasChanged = false;
	}
}