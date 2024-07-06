package fr.yukina.game.utils;

import lombok.Getter;

import java.util.*;

@Getter
public class StatisticFormatter
{
	private final Statistic         statistic;
	private final Stack<String>     texts;
	private final List<String>      patterns;
	private final Map<String, Rule> rules;

	public StatisticFormatter(Statistic statisticIn)
	{
		this.statistic = statisticIn;
		this.texts     = new Stack<>();
		this.patterns  = new ArrayList<>();
		this.rules     = new HashMap<>();
	}

	public void addPattern(String patternIn)
	{
		this.patterns.add(patternIn);
	}

	public void addRule(double minIn, double firstIn, int roundIn, double maxIn, int textMinSizeIn, int textMaxSizeIn,
	                    String... namesIn)
	{
		var rule = new Rule(minIn, firstIn, roundIn, maxIn, textMinSizeIn, textMaxSizeIn);
		for (String nameIn : namesIn)
		{
			this.rules.put(nameIn, rule);
		}
	}

	public void process()
	{
		this.texts.clear();
		for (var pattern : this.patterns)
		{
			var text = pattern;
			text = this.check(text, "iteration", this.statistic.iteration());
			text = this.check(text, "current", this.statistic.current());
			text = this.check(text, "previous", this.statistic.previous());
			text = this.check(text, "low", this.statistic.low());
			text = this.check(text, "high", this.statistic.high());
			text = this.check(text, "average", this.statistic.average());
			text = this.check(text, "total", this.statistic.total());
			text = this.check(text, "count", this.statistic.histogram().size());
			text = this.check(text, "variance", this.statistic.variance());
			text = this.check(text, "variation", this.statistic.variation());
			text = this.check(text, "deviation", this.statistic.standardDeviation());
			text = this.check(text, "range", this.statistic.range());
			text = this.check(text, "iqr", this.statistic.iqr());
			text = this.check(text, "q1", this.statistic.q1());
			text = this.check(text, "q3", this.statistic.q3());
			text = this.check(text, "median", this.statistic.median());
			text.replace("{hasChanged}", "" + this.statistic.hasChanged());
			text = this.check(text, "histogramSize", this.statistic.histogramSize());

			this.texts.push(text);
		}
	}

	private String check(String contentIn, String nameIn, double valueIn)
	{
		var rule = this.rules.get(nameIn);
		if (rule != null)
		{
			return contentIn.replace("{" + nameIn + "}", rule.format(valueIn, statistic.iteration()));
		}

		return contentIn.replace("{" + nameIn + "}", "" + valueIn);
	}

	public final static class Rule
	{
		private final double min;
		private final double first;
		private final int    round;
		private final double max;
		private final int    textMinSize;
		private final int    textMaxSize;

		private Rule(final double minIn, final double firstIn, final int roundIn, final double maxIn,
		             final int textMinSizeIn, final int textMaxSizeIn)
		{
			min         = minIn;
			first       = firstIn;
			round       = roundIn;
			max         = maxIn;
			textMinSize = textMinSizeIn;
			textMaxSize = textMaxSizeIn;
		}

		public String format(double valueIn, int iterationIn)
		{
			var value = valueIn;
			if (value < min)
			{
				value = min;
			}
			else if (value > max)
			{
				value = max;
			}
			value = Math.round(value * Math.pow(10, round)) / Math.pow(10, round);
			if (iterationIn == 0)
			{
				value = first;
			}

			var text = "" + value;
			text = text.split("\\.")[0];

			if (text.length() < this.textMinSize)
			{
				text += " ".repeat(this.textMinSize - text.length());
			}
			else if (text.length() > this.textMaxSize)
			{
				text = text.substring(0, this.textMaxSize);
			}

			return text;
		}
	}
}