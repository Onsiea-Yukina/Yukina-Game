package fr.yukina.game.world.chunk.loader.pattern;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatternManager
{
	private final Map<Integer, List<Integer>> coordinates;

	public PatternManager()
	{
		this.coordinates = new HashMap<>();
	}

	public final PatternManager add(int xIn, int zIn)
	{
		var zList = this.coordinates.get(xIn);
		if (zList == null)
		{
			zList = new java.util.ArrayList<>();
			this.coordinates.put(xIn, zList);
		}

		zList.add(zIn);
		return this;
	}

	public final PatternManager set(IPattern patternIn)
	{
		this.coordinates.clear();
		patternIn.set(this);

		return this;
	}

	public final void forEach(IPatternConsumer consumerIn)
	{
		for (int x : this.coordinates.keySet())
		{
			var zList = this.coordinates.get(x);
			for (int z : zList)
			{
				consumerIn.accept(x, z);
			}
		}
	}

	public interface IPatternConsumer
	{
		void accept(int x, int z);
	}
}