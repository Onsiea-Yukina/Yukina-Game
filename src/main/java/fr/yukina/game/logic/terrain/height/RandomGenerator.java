package fr.yukina.game.logic.terrain.height;

import lombok.Setter;

import java.util.Random;

public class RandomGenerator implements IHeightProvider
{
	private final   Random random;
	private         long   seed;
	private @Setter float  amplitude;

	public RandomGenerator(final long seedIn)
	{
		this.random    = new Random(this.seed);
		this.seed      = seedIn;
		this.amplitude = 5.0f;
	}

	public final void seed(final long seedIn)
	{
		this.seed = seedIn;
		this.random.setSeed(this.seed);
	}

	@Override
	public float get(final float xIn, final float zIn)
	{
		return this.random.nextFloat() * this.amplitude;
	}
}