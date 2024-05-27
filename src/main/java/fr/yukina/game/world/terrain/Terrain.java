package fr.yukina.game.world.terrain;

import lombok.Getter;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class Terrain
{
	public final static int WIDTH = 32;
	public final static int DEPTH = 32;

	private final @Getter int x;
	private final @Getter int y;
	private final @Getter int z;

	private final @Getter int               width;
	private final @Getter int               depth;
	private final         ITerrainGenerator generator;

	private final Map<String, Vector3f> points;

	public Terrain(int xIn, int yIn, int zIn, int widthIn, int depthIn, ITerrainGenerator generatorIn)
	{
		this.x = xIn;
		this.y = yIn;
		this.z = zIn;

		this.width = widthIn;
		this.depth = depthIn;

		this.generator = generatorIn;

		this.points = new HashMap<>();
	}

	public final void generate()
	{
		for (int x = 0; x < WIDTH; x++)
		{
			for (int z = 0; z < DEPTH; z++)
			{
				var key = key(x, z);
				if (this.points.containsKey(key))
				{
					continue;
				}

				this.points.put(key, new Vector3f(x, this.height(x, z), z));
			}
		}
	}

	public float height(float xIn, float zIn)
	{
		var key   = key(xIn, zIn);
		var point = this.points.get(key);
		if (point != null)
		{
			return point.y;
		}

		var y = this.generator.generateHeight(xIn + this.x, zIn + this.z);

		this.points.put(key, new Vector3f(xIn, y, zIn));
		return y;
	}

	public Vector3f point(int xIn, int zIn)
	{
		return this.points.get(key(xIn, zIn));
	}

	private String key(float xIn, float zIn)
	{
		return xIn + ":" + zIn;
	}

	public void cleanup()
	{
		this.points.clear();
	}
}