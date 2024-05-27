package fr.yukina.game.world.terrain;

import java.util.Random;

/**
 * <p>
 * This is Ken Perlin's implementation of Perlin Noise but modified to be more
 * OOP and configurable.
 * </p>
 * @author Ken Perlin, Matthew A. Johnston (WarmWaffles)
 */
public class PerlinNoise implements ITerrainGenerator
{
	private long   seed;
	private double amplitude;
	private double amplifier;
	private double frequency;
	private int    octaves;
	private double lacunarity;
	private double gain;
	private double xDivisor;
	private double zDivisor;

	// Constants for the noise generation
	private static final int PERMUTATION_SIZE = 8;
	private static final int TABLE_SIZE       = 1 << PERMUTATION_SIZE;
	private static final int TABLE_MASK       = TABLE_SIZE - 1;
	private static final int POINTS_SIZE      = 32;

	// Permutation table and gradient vectors
	private int[]      permutationTable;
	private double[][] gradient2D;
	private double[]   gradient1D;

	// Precomputed points for 3D gradient
	private static double[][] gradientPoints;

	/**
	 * Constructor for PerlinNoise class. Initializes with the given seed and configuration.
	 * @param seedIn      the seed for random number generation
	 * @param amplitude   the amplitude for noise generation
	 * @param frequency   the frequency for noise generation
	 * @param persistence the persistence for noise generation
	 * @param octaves     the number of octaves for noise generation
	 * @param lacunarity  the lacunarity for noise generation
	 * @param gain        the gain for noise generation
	 */
	public PerlinNoise(long seedIn, double amplitude, double amplifier, double frequency, int octaves,
	                   double lacunarity, double gain, double xDivisor, double zDivisor)
	{
		this.amplitude  = amplitude;
		this.amplifier  = amplifier;
		this.frequency  = frequency;
		this.octaves    = octaves;
		this.lacunarity = lacunarity;
		this.gain       = gain;
		this.xDivisor   = xDivisor;
		this.zDivisor   = zDivisor;

		permutationTable = new int[TABLE_SIZE + TABLE_SIZE + 2];
		gradient2D       = new double[TABLE_SIZE + TABLE_SIZE + 2][2];
		gradient1D       = new double[TABLE_SIZE + TABLE_SIZE + 2];

		gradientPoints = new double[POINTS_SIZE][3];

		init(seedIn);
	}

	/**
	 * Sets the seed for the noise generator and reinitializes the generator.
	 * @param seed the seed to set
	 */
	public void setSeed(int seed)
	{
		init(seed);
	}

	/**
	 * Generates 3D Perlin noise.
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param z the z-coordinate
	 * @return the noise value at the given coordinates
	 */
	public double noise(double x, double y, double z)
	{
		double total = 0.0;
		double freq  = frequency;
		double amp   = amplitude;

		for (int i = 0; i < octaves; i++)
		{
			total += singleNoise(x * freq, y * freq, z * freq) * amp;
			freq *= lacunarity;
			amp *= gain;
		}

		return total;
	}

	/**
	 * Generates 2D Perlin noise.
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @return the noise value at the given coordinates
	 */
	public double noise(double x, double y)
	{
		double total = 0.0;
		double freq  = frequency;
		double amp   = amplitude;

		for (int i = 0; i < octaves; i++)
		{
			total += singleNoise(x * freq, y * freq) * amp;
			freq *= lacunarity;
			amp *= gain;
		}

		return total;
	}

	/**
	 * Generates 1D Perlin noise.
	 * @param x the x-coordinate
	 * @return the noise value at the given coordinate
	 */
	public double noise(double x)
	{
		double total = 0.0;
		double freq  = frequency;
		double amp   = amplitude;

		for (int i = 0; i < octaves; i++)
		{
			total += singleNoise(x * freq) * amp;
			freq *= lacunarity;
			amp *= gain;
		}

		return total;
	}

	/**
	 * Gets the seed used for noise generation.
	 * @return the seed
	 */
	public long getSeed()
	{
		return seed;
	}

	// ========================================================================
	//                             PRIVATE METHODS
	// ========================================================================

	/**
	 * Normalizes a 2D vector.
	 * @param vector the vector to normalize
	 */
	private void normalize2D(double[] vector)
	{
		double magnitude = Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
		vector[0] /= magnitude;
		vector[1] /= magnitude;
	}

	/**
	 * Retrieves the gradient vector from the precomputed points.
	 * @param index the index to retrieve
	 * @return the gradient vector
	 */
	private double[] gradient(int index)
	{
		return gradientPoints[index % POINTS_SIZE];
	}

	/**
	 * Smooth curve function for Perlin noise.
	 * @param t the input value
	 * @return the smoothed value
	 */
	private double smoothCurve(double t)
	{
		return t * t * (3 - 2 * t);
	}

	/**
	 * Linear interpolation function.
	 * @param t the interpolation factor
	 * @param a the start value
	 * @param b the end value
	 * @return the interpolated value
	 */
	private double lerp(double t, double a, double b)
	{
		return a + t * (b - a);
	}

	/**
	 * Initializes the Perlin noise generator with the given seed.
	 * @param seed the seed for initialization
	 */
	private void init(long seed)
	{
		this.seed = seed;

		int    i, j, k;
		double u, v, w, U, V, W, Hi, Lo;
		Random random = new Random(seed);

		// Initialize permutation table and gradient vectors
		for (i = 0; i < TABLE_SIZE; i++)
		{
			permutationTable[i] = i;
			gradient1D[i]       = 2 * random.nextDouble() - 1;

			do
			{
				u = 2 * random.nextDouble() - 1;
				v = 2 * random.nextDouble() - 1;
			} while (u * u + v * v > 1 || Math.abs(u) > 2.5 * Math.abs(v) || Math.abs(v) > 2.5 * Math.abs(u)
			         || Math.abs(Math.abs(u) - Math.abs(v)) < .4);

			gradient2D[i][0] = u;
			gradient2D[i][1] = v;

			normalize2D(gradient2D[i]);

			do
			{
				u  = 2 * random.nextDouble() - 1;
				v  = 2 * random.nextDouble() - 1;
				w  = 2 * random.nextDouble() - 1;
				U  = Math.abs(u);
				V  = Math.abs(v);
				W  = Math.abs(w);
				Lo = Math.min(U, Math.min(V, W));
				Hi = Math.max(U, Math.max(V, W));
			} while (u * u + v * v + w * w > 1 || Hi > 4 * Lo
			         || Math.min(Math.abs(U - V), Math.min(Math.abs(U - W), Math.abs(V - W))) < .2);
		}

		// Shuffle the permutation table
		while (--i > 0)
		{
			k                   = permutationTable[i];
			j                   = (int) (random.nextLong() & TABLE_MASK);
			permutationTable[i] = permutationTable[j];
			permutationTable[j] = k;
		}

		// Extend the permutation table
		for (i = 0; i < TABLE_SIZE + 2; i++)
		{
			permutationTable[TABLE_SIZE + i] = permutationTable[i];
			gradient1D[TABLE_SIZE + i]       = gradient1D[i];
			for (j = 0; j < 2; j++)
			{
				gradient2D[TABLE_SIZE + i][j] = gradient2D[i][j];
			}
		}

		// Precompute gradient points for 3D noise
		gradientPoints[3][0] = gradientPoints[3][1] = gradientPoints[3][2] = Math.sqrt(1. / 3);
		double r2 = Math.sqrt(1. / 2);
		double s  = Math.sqrt(2 + r2 + r2);

		for (i = 0; i < 3; i++)
		{
			for (j = 0; j < 3; j++)
			{
				gradientPoints[i][j] = (i == j ? 1 + r2 + r2 : r2) / s;
			}
		}

		for (i = 0; i <= 1; i++)
		{
			for (j = 0; j <= 1; j++)
			{
				for (k = 0; k <= 1; k++)
				{
					int n = i + j * 2 + k * 4;
					if (n > 0)
					{
						for (int m = 0; m < 4; m++)
						{
							gradientPoints[4 * n + m][0] = (i == 0 ? 1 : -1) * gradientPoints[m][0];
							gradientPoints[4 * n + m][1] = (j == 0 ? 1 : -1) * gradientPoints[m][1];
							gradientPoints[4 * n + m][2] = (k == 0 ? 1 : -1) * gradientPoints[m][2];
						}
					}
				}
			}
		}
	}

	/**
	 * Generates the height for a given x and z coordinate based on Perlin noise.
	 * @param xIn the x-coordinate
	 * @param zIn the z-coordinate
	 * @return the generated height
	 */
	@Override
	public float generateHeight(final float xIn, final float zIn)
	{
		var height = noise(zIn / this.xDivisor, xIn / this.zDivisor, 0.0D) * this.amplifier;
		return (float) height;
	}

	/**
	 * Generates single octave of 3D Perlin noise.
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @param z the z-coordinate
	 * @return the noise value at the given coordinates
	 */
	private double singleNoise(double x, double y, double z)
	{
		int      ix, iy, iz, i0, i1, i00, i10, i01, i11;
		double   fx0, fx1, fy0, fy1, fz, sx, sy, sz, a, b, c, d, u, v;
		double[] q;

		ix = (int) Math.IEEEremainder(Math.floor(x), TABLE_SIZE);
		if (ix < 0)
		{
			ix += TABLE_SIZE;
		}

		fx0 = x - Math.floor(x);
		fx1 = fx0 - 1;

		iy = (int) Math.IEEEremainder(Math.floor(y), TABLE_SIZE);
		if (iy < 0)
		{
			iy += TABLE_SIZE;
		}

		fy0 = y - Math.floor(y);
		fy1 = fy0 - 1;

		iz = (int) Math.IEEEremainder(Math.floor(z), TABLE_SIZE);
		if (iz < 0)
		{
			iz += TABLE_SIZE;
		}

		fz = z - Math.floor(z);

		i0 = permutationTable[ix];

		ix++;

		i1 = permutationTable[ix];

		i00 = permutationTable[i0 + iy];
		i10 = permutationTable[i1 + iy];

		iy++;

		i01 = permutationTable[i0 + iy];
		i11 = permutationTable[i1 + iy];

		sx = smoothCurve(fx0);
		sy = smoothCurve(fy0);
		sz = smoothCurve(fz);

		q = gradient(i00 + iz);
		u = fx0 * q[0] + fy0 * q[1] + fz * q[2];
		q = gradient(i10 + iz);
		v = fx1 * q[0] + fy0 * q[1] + fz * q[2];
		a = lerp(sx, u, v);
		q = gradient(i01 + iz);
		u = fx0 * q[0] + fy1 * q[1] + fz * q[2];
		q = gradient(i11 + iz);
		v = fx1 * q[0] + fy1 * q[1] + fz * q[2];
		b = lerp(sx, u, v);
		c = lerp(sy, a, b);

		iz++;
		fz--;

		q = gradient(i00 + iz);
		u = fx0 * q[0] + fy0 * q[1] + fz * q[2];
		q = gradient(i10 + iz);
		v = fx1 * q[0] + fy0 * q[1] + fz * q[2];
		a = lerp(sx, u, v);
		q = gradient(i01 + iz);
		u = fx0 * q[0] + fy1 * q[1] + fz * q[2];
		q = gradient(i11 + iz);
		v = fx1 * q[0] + fy1 * q[1] + fz * q[2];
		b = lerp(sx, u, v);
		d = lerp(sy, a, b);

		return lerp(sz, c, d);
	}

	/**
	 * Generates single octave of 2D Perlin noise.
	 * @param x the x-coordinate
	 * @param y the y-coordinate
	 * @return the noise value at the given coordinates
	 */
	private double singleNoise(double x, double y)
	{
		int      ix0, ix1, iy0, iy1, i00, i10, i01, i11;
		double   fx0, fx1, fy0, fy1, sx, sy, a, b, t, u, v;
		double[] q;

		t   = x + TABLE_SIZE;
		ix0 = ((int) t) & TABLE_MASK;
		ix1 = (ix0 + 1) & TABLE_MASK;
		fx0 = t - (int) t;
		fx1 = fx0 - 1;

		t   = y + TABLE_SIZE;
		iy0 = ((int) t) & TABLE_MASK;
		iy1 = (iy0 + 1) & TABLE_MASK;
		fy0 = t - (int) t;
		fy1 = fy0 - 1;

		int i = permutationTable[ix0];
		int j = permutationTable[ix1];

		i00 = permutationTable[i + iy0];
		i10 = permutationTable[j + iy0];
		i01 = permutationTable[i + iy1];
		i11 = permutationTable[j + iy1];

		sx = smoothCurve(fx0);
		sy = smoothCurve(fy0);

		q = gradient2D[i00];
		u = fx0 * q[0] + fy0 * q[1];
		q = gradient2D[i10];
		v = fx1 * q[0] + fy0 * q[1];
		a = lerp(sx, u, v);

		q = gradient2D[i01];
		u = fx0 * q[0] + fy1 * q[1];
		q = gradient2D[i11];
		v = fx1 * q[0] + fy1 * q[1];
		b = lerp(sx, u, v);

		return lerp(sy, a, b);
	}

	/**
	 * Generates single octave of 1D Perlin noise.
	 * @param x the x-coordinate
	 * @return the noise value at the given coordinate
	 */
	private double singleNoise(double x)
	{
		int    ix0, ix1;
		double fx0, fx1, sx, t, u, v;

		t   = x + TABLE_SIZE;
		ix0 = ((int) t) & TABLE_MASK;
		ix1 = (ix0 + 1) & TABLE_MASK;
		fx0 = t - (int) t;
		fx1 = fx0 - 1;

		sx = smoothCurve(fx0);
		u  = fx0 * gradient1D[permutationTable[ix0]];
		v  = fx1 * gradient1D[permutationTable[ix1]];

		return lerp(sx, u, v);
	}
}