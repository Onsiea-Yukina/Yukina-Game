package fr.yukina.game.logic.terrain.height;

import lombok.Setter;

import java.util.Random;

public class NoiseGenerator implements IHeightProvider
{
	private final   Random random;
	private         long   seed;
	private @Setter int    octaves;
	private @Setter float  roughness;
	private @Setter float  amplitude;

	public NoiseGenerator(final long seedIn, final int octavesIn, final float roughnessIn)
	{
		this.random    = new Random(this.seed);
		this.seed      = seedIn;
		this.octaves   = octavesIn;
		this.roughness = roughnessIn;
	}

	public final void seed(final long seedIn)
	{
		this.seed = seedIn;
		this.random.setSeed(this.seed);
	}

	public float height(final float xIn, final float zIn)
	{
		float total = 0.0f;
		float d     = (float) Math.pow(2, this.octaves - 1);

		for (int i = 0; i < this.octaves; i++)
		{
			float freq = (float) (Math.pow(2, i) / d);
			float amp  = (float) Math.pow(this.roughness, i) * this.amplitude;
			total += interpolatedNoise(xIn * freq, zIn * freq) * amp;
		}

		return total;
	}

	private float interpolatedNoise(float xIn, float zIn)
	{
		int   intX  = (int) xIn;
		int   intZ  = (int) zIn;
		float fracX = xIn - intX;
		float fracZ = zIn - intZ;

		float v1 = smoothNoise(intX, intZ);
		float v2 = smoothNoise(intX + 1, intZ);
		float v3 = smoothNoise(intX, intZ + 1);
		float v4 = smoothNoise(intX + 1, intZ + 1);
		float i1 = interpolate(v1, v2, fracX);
		float i2 = interpolate(v3, v4, fracX);
		return interpolate(i1, i2, fracZ);
	}

	public final float interpolate(float aIn, float bIn, float blendIn)
	{
		double theta = blendIn * Math.PI;
		float  f     = (1.0f - (float) Math.cos(theta)) / 2.0f;
		return aIn * (1.0f - f)
		       + bIn * f; // (float) (aIn * ((1.0f - Math.cos(theta)) / 2.0f) + (bIn * (Math.sin(theta) / 2.0f))) /
		// 2.0f
	}

	public final float smoothNoise(final int xIn, final int zIn)
	{
		float corners =
				(noise(xIn - 1, zIn - 1) + noise(xIn + 1, zIn - 1) + noise(xIn - 1, zIn + 1) + noise(xIn + 1, zIn + 1))
				/ 16f;
		float sides  = (noise(xIn - 1, zIn) + noise(xIn + 1, zIn) + noise(xIn, zIn - 1) + noise(xIn, zIn + 1)) / 8f;
		float center = noise(xIn, zIn) / 4f;
		return corners + sides + center;
	}

	public final float noise(final int xIn, final int zIn)
	{
		this.random.setSeed(xIn * 49632 + zIn * 325176 + this.seed);

		return this.random.nextFloat() * 2.0f - 1.0f;
	}

	@Override
	public float get(final float xIn, final float zIn)
	{
		return this.height(xIn, zIn);
	}
}