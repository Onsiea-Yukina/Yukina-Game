package fr.yukina.game.world.chunk.loader;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.IChunk;
import fr.yukina.game.world.terrain.Terrain;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class QueueChunkLoader implements IChunkLoader
{
	private static final Logger LOGGER = Logger.getLogger(QueueChunkLoader.class.getName());

	private final ChunkManager                     chunkManager;
	private final Queue<ChunkManager.ChunkLoading> chunkLoadingQueue;

	public QueueChunkLoader(ChunkManager chunkManagerIn)
	{
		this.chunkManager      = chunkManagerIn;
		this.chunkLoadingQueue = new LinkedList<>();
	}

	public final void update()
	{
		this.updateStates();

		var start = System.nanoTime();

		// Check unload and visibility of chunks
		unloadAndCheckVisibilityOfChunks();

		// Clear queue
		this.chunkLoadingQueue.clear();

		// Load chunks
		loadChunksInCircularPattern();

		// Load chunks based on priority
		while (!this.chunkLoadingQueue.isEmpty())
		{
			ChunkManager.ChunkLoading chunkLoading = this.chunkLoadingQueue.poll();
			if (chunkLoading != null)
			{
				chunkLoading.loadFunction().get();
				synchronized (this.chunkManager.chunks())
				{
					validateLoadedChunk(chunkLoading);
				}
			}
		}
		this.resetStates();
	}

	private void unloadAndCheckVisibilityOfChunks()
	{
		int   centerX            = (int) this.chunkManager.player().camera().position().x / Terrain.WIDTH;
		int   centerZ            = (int) this.chunkManager.player().camera().position().z / Terrain.DEPTH;
		int   renderDistance     = (int) this.chunkManager.renderDistance().current();
		float maxDistanceSquared = (renderDistance * Terrain.WIDTH) * (renderDistance * Terrain.WIDTH);

		for (var chunk : this.chunkManager.chunks().values())
		{
			float dx              = centerX - (chunk.terrain().x() / Terrain.WIDTH);
			float dz              = centerZ - (chunk.terrain().z() / Terrain.DEPTH);
			float distanceSquared = dx * dx * Terrain.WIDTH * Terrain.WIDTH + dz * dz * Terrain.DEPTH * Terrain.DEPTH;

			if (distanceSquared > maxDistanceSquared)
			{
				chunk.visible(false);
				chunk.needUnload(true);
				chunk.cleanup();
				this.chunkManager.chunks().remove(chunk.key());
				for (var listener : this.chunkManager.unloadedListeners())
				{
					listener.onChunk(chunk);
				}
				LOGGER.info(String.format("Unloaded chunk at (%.2f, %.2f) with key %s", dx, dz, chunk.key()));
			}
			else
			{
				chunk.visible(!this.chunkManager.isOutFrustum(chunk));
			}
		}
	}

	private void loadChunksInCircularPattern()
	{
		int   centerX            = (int) this.chunkManager.player().camera().position().x / Terrain.WIDTH;
		int   centerZ            = (int) this.chunkManager.player().camera().position().z / Terrain.DEPTH;
		int   renderDistance     = (int) this.chunkManager.renderDistance().current();
		float maxDistanceSquared = (renderDistance * Terrain.WIDTH) * (renderDistance * Terrain.WIDTH);

		for (int x = centerX - renderDistance; x <= centerX + renderDistance; x++)
		{
			for (int z = centerZ - renderDistance; z <= centerZ + renderDistance; z++)
			{
				float dx = centerX - x;
				float dz = centerZ - z;
				float distanceSquared =
						dx * dx * Terrain.WIDTH * Terrain.WIDTH + dz * dz * Terrain.DEPTH * Terrain.DEPTH;

				if (distanceSquared > maxDistanceSquared)
				{
					continue;
				}

				if (this.chunkManager.renderDistance().isOut(x * Terrain.WIDTH, z * Terrain.DEPTH))
				{
					continue;
				}

				String key = this.chunkManager.key(x, z);

				if (!this.chunkManager.chunks().containsKey(key))
				{
					final int finalX = x;
					final int finalZ = z;

					this.chunkLoadingQueue.add(new ChunkManager.ChunkLoading(x, z, () ->
					{
						LOGGER.info(String.format("Loading chunk at %s", key));
						return this.chunkManager.loadChunk(key, finalX, finalZ);
					}, key));
				}
			}
		}
	}

	public final void resetStates()
	{
		// No need to reset any states in this simplified version
	}

	public final void updateStates()
	{
		// Ensure the frustum intersection is updated
		this.chunkManager.frustumIntersection().set(this.chunkManager.player().camera().projectionViewMatrix());
	}

	public void cleanup()
	{
		// Implement any necessary cleanup logic here
	}

	private void validateLoadedChunk(ChunkManager.ChunkLoading chunkLoading)
	{
		String key = chunkLoading.x() + ":" + chunkLoading.z();
		if (!chunkLoading.key().contentEquals(key))
		{
			LOGGER.warning(
					String.format("Chunk key %s does not match expected key %s after loading", chunkLoading.key(),
					              key));
		}
		if (!chunkManager.chunks().containsKey(key))
		{
			LOGGER.warning(String.format("Chunk %s not found after loading", key));
		}
		else
		{
			IChunk chunk = chunkManager.chunks().get(key);
			if (chunkManager.renderDistance().isOut(chunk))
			{
				LOGGER.warning(String.format("Chunk %s is out of render distance after loading", key));
			}
			if (chunkManager.isOutFrustum(chunk))
			{
				LOGGER.info(String.format("Chunk %s is out of frustum after loading", key));
			}
		}
	}
}