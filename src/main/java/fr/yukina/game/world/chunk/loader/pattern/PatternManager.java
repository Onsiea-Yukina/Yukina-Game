package fr.yukina.game.world.chunk.loader.pattern;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PatternManager
{
	private final Map<Integer, Queue<Integer>> coordinates;

	public PatternManager()
	{
		this.coordinates = new ConcurrentHashMap<>();
	}

	public final PatternManager add(int xIn, int zIn)
	{
		var zList = this.coordinates.get(xIn);
		if (zList == null)
		{
			zList = new ConcurrentLinkedQueue<>();
			this.coordinates.put(xIn, zList);
		}
		else
		{
			for (var z : zList)
			{
				if (z == zIn)
				{
					return this;
				}
			}
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
		synchronized (this.coordinates)
		{
			for (int x : this.coordinates.keySet())
			{
				var zList = this.coordinates.get(x);
				if (zList == null)
				{
					continue;
				}

				for (int z : zList)
				{
					consumerIn.accept(x, z);
				}
			}
		}
	}

	public interface IPatternConsumer
	{
		void accept(int x, int z);
	}
}