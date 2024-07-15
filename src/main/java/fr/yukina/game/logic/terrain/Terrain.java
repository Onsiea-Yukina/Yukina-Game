package fr.yukina.game.logic.terrain;

import fr.yukina.game.logic.terrain.height.FlatGenerator;
import fr.yukina.game.logic.terrain.height.IHeightProvider;
import fr.yukina.game.utils.Maths;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2f;
import org.joml.Vector3f;

@Getter
public class Terrain implements IHeightProvider
{
	public final static int WIDTH = 256;
	public final static int DEPTH = 256;

	private final float x;
	private final float y;
	private final float z;

	private @Setter float     min;
	private @Setter float     max;
	private final   float[][] heights;

	private int synchronizationId;

	public Terrain(float xIn, float yIn, float zIn, IHeightProvider heightProviderIn)
	{
		this.x       = xIn;
		this.y       = yIn;
		this.z       = zIn;
		this.min     = Float.NEGATIVE_INFINITY;
		this.max     = Float.POSITIVE_INFINITY;
		this.heights = new float[WIDTH][DEPTH];

		for (int x = 0; x < this.heights.length; x++)
		{
			for (int z = 0; z < this.heights[0].length; z++)
			{
				this.heights[x][z] = heightProviderIn.get(x, z);
			}
		}
	}

	public Terrain(float xIn, float yIn, float zIn)
	{
		this(xIn, yIn, zIn, new FlatGenerator(0.0f));
	}

	public final float averageGet(float worldXIn, float worldZIn)
	{
		float terrainX       = worldXIn - this.x;
		float terrainZ       = worldZIn - this.z;
		float gridSquareSize = (Terrain.WIDTH) / ((float) this.heights.length - 1.0f);
		int   gridX          = (int) Math.floor(terrainX / gridSquareSize);
		int   gridZ          = (int) Math.floor(terrainZ / gridSquareSize);
		if (gridX >= this.heights.length - 1 || gridZ >= this.heights.length - 1 || gridX < 0 || gridZ < 0)
		{
			return 0.0f;
		}
		float xCoord = (terrainX % gridSquareSize) / gridSquareSize;
		float zCoord = (terrainZ % gridSquareSize) / gridSquareSize;
		float answer;
		if (xCoord <= (1 - zCoord))
		{
			answer = Maths.barryCentric(new Vector3f(0, heights[gridX][gridZ], 0),
			                            new Vector3f(1, heights[gridX + 1][gridZ], 0),
			                            new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		}
		else
		{
			answer = Maths.barryCentric(new Vector3f(1, heights[gridX + 1][gridZ], 0),
			                            new Vector3f(1, heights[gridX + 1][gridZ + 1], 1),
			                            new Vector3f(0, heights[gridX][gridZ + 1], 1), new Vector2f(xCoord, zCoord));
		}
		return answer;
	}

	public void set(float xIn, float zIn, float heightIn)
	{
		if (xIn > WIDTH || xIn < 0 || zIn > DEPTH || zIn < 0 || xIn > this.heights.length
		    || zIn > this.heights[0].length)
		{
			throw new IllegalArgumentException(
					"[ERROR] Terrain : vertex out of bounds " + xIn + ", " + zIn + " > " + WIDTH + "[" + heights.length
					+ "], " + DEPTH + "[" + heights[0].length + "]");
		}
		this.heights[(int) xIn][(int) zIn] = Math.min(Math.max(heightIn, this.min), this.max);
	}

	public float get(float xIn, float zIn)
	{
		if (xIn > WIDTH || xIn < 0 || zIn > DEPTH || zIn < 0 || xIn > this.heights.length
		    || zIn > this.heights[0].length)
		{
			throw new IllegalArgumentException(
					"[ERROR] Terrain : vertex out of bounds " + xIn + ", " + zIn + " > " + WIDTH + "[" + heights.length
					+ "], " + DEPTH + "[" + heights[0].length + "]");
		}

		return this.heights[(int) xIn][(int) zIn];
	}

	public final void increaseSynchronizationId()
	{
		this.synchronizationId++;
	}
}