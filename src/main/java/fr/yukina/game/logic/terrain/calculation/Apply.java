package fr.yukina.game.logic.terrain.calculation;

import fr.yukina.game.logic.terrain.height.IHeightCollector;
import lombok.Getter;

@Getter
public class Apply
{
	private final Calculation      calculation;
	private final IHeightCollector destination;
	private final long             start;

	public Apply(Calculation calculationIn, IHeightCollector destinationIn)
	{
		this.calculation = calculationIn;
		this.destination = destinationIn;
		this.start       = System.nanoTime();
	}

	public final void apply()
	{
		this.calculation.stop();
		for (var xEntry : this.calculation.heights().entrySet())
		{
			var x = xEntry.getKey();
			for (var zEntry : xEntry.getValue().entrySet())
			{
				var z = zEntry.getKey();
				var y = zEntry.getValue();
				this.destination.collect(x, z, y);
			}
		}
		this.calculation.clear();
	}
}