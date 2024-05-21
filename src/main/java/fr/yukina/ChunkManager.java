package fr.yukina;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.joml.FrustumIntersection;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

public class ChunkManager
{
	public static final int CHUNK_SIZE = 16;

	private Map<String, Chunk> chunks;
	private int                textureArrayId;
	private PerlinNoise        perlinNoise;
	private Camera             camera;
	private int                currentChunkX, currentChunkZ;
	private Stack<ChunkToLoad> needToLoadChunks = new Stack<>();

	@Getter
	@Setter
	@AllArgsConstructor
	public final static class ChunkToLoad
	{
		private final String key;
		private final int    x;
		private final int    z;
	}

	public ChunkManager(int textureArrayId, PerlinNoise perlinNoise, Camera camera)
	{
		this.textureArrayId = textureArrayId;
		this.perlinNoise    = perlinNoise;
		this.camera         = camera;
		this.chunks         = new HashMap<>();
		this.currentChunkX  = Integer.MIN_VALUE;
		this.currentChunkZ  = Integer.MIN_VALUE;
	}

	public void update()
	{
		Vector3f cameraPos = camera.position();
		int      newChunkX = (int) cameraPos.x / CHUNK_SIZE;
		int      newChunkZ = (int) cameraPos.z / CHUNK_SIZE;

		if (newChunkX != currentChunkX || newChunkZ != currentChunkZ)
		{
			currentChunkX = newChunkX;
			currentChunkZ = newChunkZ;

			this.needToLoadChunks.clear();
			unloadFarChunks(newChunkX, newChunkZ);
			new Thread(() ->
			           {
				           synchronized (this.needToLoadChunks)
				           {
					           for (int x = currentChunkX - TerrainRenderer.RENDER_DISTANCE / 2;
					                x <= currentChunkX + TerrainRenderer.RENDER_DISTANCE / 2; x++)
					           {
						           for (int z = currentChunkZ - TerrainRenderer.RENDER_DISTANCE / 2;
						                z <= currentChunkZ + TerrainRenderer.RENDER_DISTANCE / 2; z++)
						           {
							           this.needToLoadChunks.push(new ChunkToLoad(getChunkKey(x, z), x, z));
						           }
					           }
				           }
			           }).start();
		}

		synchronized (this.needToLoadChunks)
		{
			if (!this.needToLoadChunks.isEmpty())
			{
				loadChunksAround(newChunkX, newChunkZ);
			}
		}
	}

	private void loadChunksAround(int chunkX, int chunkZ)
	{
		var start = System.nanoTime();
		synchronized (this.needToLoadChunks)
		{
			while (!this.needToLoadChunks.isEmpty())
			{
				var chunkToLoad = this.needToLoadChunks.pop();
				var x           = chunkToLoad.x;
				var z           = chunkToLoad.z;
				var chunkKey    = chunkToLoad.key;
				if (!chunks.containsKey(chunkKey))
				{
					chunks.put(chunkKey, new Chunk(x * CHUNK_SIZE, z * CHUNK_SIZE, textureArrayId, perlinNoise));
				}

				if (System.nanoTime() - start > 50_000_000L)
				{
					return;
				}
			}
		}
	}

	private void unloadFarChunks(int chunkX, int chunkZ)
	{
		Iterator<Map.Entry<String, Chunk>> iterator = chunks.entrySet().iterator();
		while (iterator.hasNext())
		{
			Map.Entry<String, Chunk> entry  = iterator.next();
			String[]                 coords = entry.getKey().split(",");
			int                      x      = Integer.parseInt(coords[0]);
			int                      z      = Integer.parseInt(coords[1]);

			if (Math.abs(x - chunkX) > TerrainRenderer.RENDER_DISTANCE / 2
			    || Math.abs(z - chunkZ) > TerrainRenderer.RENDER_DISTANCE / 2)
			{
				iterator.remove();
			}
		}
	}

	private String getChunkKey(int x, int z)
	{
		return x + "," + z;
	}

	public void renderChunks(FrustumIntersection frustumIntersection)
	{
		for (Chunk chunk : chunks.values())
		{
			if (frustumIntersection.testAab(chunk.x(), 0, chunk.z(), chunk.x() + CHUNK_SIZE, 1,
			                                chunk.z() + CHUNK_SIZE))
			{
				chunk.render();
			}
		}
	}
}