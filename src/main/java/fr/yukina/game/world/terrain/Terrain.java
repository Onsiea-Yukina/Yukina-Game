package fr.yukina.game.world.terrain;

import lombok.Getter;
import lombok.Setter;
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

	private @Setter @Getter float lod;

	private final @Getter int               width;
	private final @Getter int               depth;
	private final         ITerrainGenerator generator;

	private final @Getter Map<String, Vector3f> points;

	public Terrain(int xIn, int yIn, int zIn, int widthIn, int depthIn, ITerrainGenerator generatorIn, float lodIn)
	{
		this.x = xIn;
		this.y = yIn;
		this.z = zIn;

		this.width = widthIn;
		this.depth = depthIn;
		this.lod   = validateLOD(lodIn, widthIn, depthIn);

		this.generator = generatorIn;

		this.points = new HashMap<>();
	}

	private float validateLOD(float lodIn, int width, int depth)
	{
		if (lodIn <= 0)
		{
			return 1;
		}
		while (width % lodIn != 0 || depth % lodIn != 0)
		{
			lodIn--;
		}
		return lodIn;
	}

	public final void generate()
	{
		for (float x = 0.0f; x <= WIDTH; x += this.lod)
		{
			for (float z = 0.0f; z <= DEPTH; z += this.lod)
			{
				var key = this.key(x, z);
				if (this.points.containsKey(key))
				{
					continue;
				}

				this.points.put(key, new Vector3f(x, this.height(x, z), z));
			}
		}
	}

	// TODO optimize to avoid multiple map check
	public boolean containsKey(float xIn, float zIn)
	{
		var key = this.key(xIn, zIn);
		return this.points.containsKey(key);
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

	public Vector3f point(float xIn, float zIn)
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