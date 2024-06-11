package fr.yukina.game.world.chunk.loader.component;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.terrain.Terrain;

import java.util.Queue;

public class ChunkLoaderContext
{
	private final ChunkManager                     chunkManager;
	private final Queue<ChunkManager.ChunkLoading> chunkLoadingQueue;

	private Integer centerX;
	private Integer centerZ;
	private Integer renderDistance;
	private Float   maxDistanceSquared;

	public ChunkLoaderContext(ChunkManager chunkManager, Queue<ChunkManager.ChunkLoading> chunkLoadingQueue)
	{
		this.chunkManager      = chunkManager;
		this.chunkLoadingQueue = chunkLoadingQueue;
	}

	public ChunkManager getChunkManager()
	{
		return chunkManager;
	}

	public Queue<ChunkManager.ChunkLoading> getChunkLoadingQueue()
	{
		return chunkLoadingQueue;
	}

	public int getCenterX()
	{
		if (centerX == null)
		{
			centerX = (int) chunkManager.player().camera().position().x / Terrain.WIDTH;
		}
		return centerX;
	}

	public int getCenterZ()
	{
		if (centerZ == null)
		{
			centerZ = (int) chunkManager.player().camera().position().z / Terrain.DEPTH;
		}
		return centerZ;
	}

	public int getRenderDistance()
	{
		if (renderDistance == null)
		{
			renderDistance = (int) chunkManager.renderDistance().current();
		}
		return renderDistance;
	}

	public float getMaxDistanceSquared()
	{
		if (maxDistanceSquared == null)
		{
			maxDistanceSquared = (float) ((getRenderDistance() * Terrain.WIDTH) * (getRenderDistance()
			                                                                       * Terrain.WIDTH));
		}
		return maxDistanceSquared;
	}
}
