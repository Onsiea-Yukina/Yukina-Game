package fr.yukina.game.world.chunk;

import fr.yukina.game.world.terrain.ITerrainGenerator;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class Chunk implements IChunk
{
	private static final Map<String, Chunk> CHUNKS = new ConcurrentHashMap<>();

	public static final IChunk get(String keyIn, int xIn, int yIn, int zIn, int widthIn, int depthIn,
	                               ITerrainGenerator generatorIn)
	{
		var chunk = CHUNKS.get(keyIn);
		if (chunk == null)
		{
			chunk = new Chunk(keyIn, xIn, yIn, zIn, widthIn, depthIn, generatorIn);
			CHUNKS.put(keyIn, chunk);
		}

		return new ChunkLink(chunk);
	}

	private static final int HEIGHT = 16;

	private final   String   key;
	private final   Terrain  terrain;
	private @Setter boolean  visible;
	private @Setter boolean  needUnload;
	private final   Vector3f min;
	private final   Vector3f max;

	private Chunk(String keyIn, int xIn, int yIn, int zIn, int widthIn, int depthIn, ITerrainGenerator generatorIn)
	{
		this.key        = keyIn;
		this.terrain    = new Terrain(xIn, yIn, zIn, widthIn, depthIn, generatorIn);
		this.min        = new Vector3f(xIn, yIn, zIn);
		this.max        = new Vector3f(xIn + widthIn, yIn + HEIGHT, zIn + depthIn);
		this.visible    = true;
		this.needUnload = false;
	}

	public final void generate()
	{
		this.terrain.generate();
	}

	public final void cleanup()
	{
		this.terrain.cleanup();
	}
}