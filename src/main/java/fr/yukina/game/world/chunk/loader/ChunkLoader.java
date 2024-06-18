package fr.yukina.game.world.chunk.loader;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.IChunk;
import fr.yukina.game.world.terrain.Terrain;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

public class ChunkLoader implements IChunkLoader
{
	private static final Logger LOGGER = Logger.getLogger(ChunkLoader.class.getName());

	private final ChunkManager                     chunkManager;
	private final Queue<ChunkManager.ChunkLoading> chunkLoadingQueue;
	private       long                             chunkLoadingTime = 0L;
	private       boolean                          needCheckLoad;
	private       boolean                          needCheckVisibility;
	private       boolean                          needCheckUnload;

	public ChunkLoader(ChunkManager chunkManagerIn)
	{
		this.chunkManager      = chunkManagerIn;
		this.chunkLoadingQueue = new PriorityQueue<>(Comparator.comparingInt(chunkLoading ->
		                                                                     {
			                                                                     if (chunkManager.isOutFrustum(
					                                                                     chunkLoading.x(),
					                                                                     chunkLoading.z()))
			                                                                     {
				                                                                     return 1;
			                                                                     }
			                                                                     else
			                                                                     {
				                                                                     return -1;
			                                                                     }
		                                                                     }));
	}

	public final void update()
	{
		this.updateStates();

		var start = System.nanoTime();
		// Check unload and visibility of chunks
		if (this.needCheckUnload || this.needCheckVisibility)
		{
			unloadAndCheckVisibilityOfChunks();
		}

		if (this.needCheckLoad)
		{
			// Clear queue
			synchronized (this.chunkLoadingQueue)
			{
				this.chunkLoadingQueue.clear();
			}

			// Load chunks
			loadChunksInCircularPattern();
		}

		this.chunkLoadingTime = System.nanoTime() - start;

		// Load chunks based on priority
		while (!this.chunkLoadingQueue.isEmpty() && this.chunkLoadingTime < 16_000_000L / 2)
		{
			ChunkManager.ChunkLoading chunkLoading;
			synchronized (this.chunkLoadingQueue)
			{
				chunkLoading = this.chunkLoadingQueue.poll();
			}
			if (chunkLoading != null)
			{
				chunkLoading.loadFunction().get();
				synchronized (this.chunkManager.chunks())
				{
					validateLoadedChunk(chunkLoading);
				}
			}
			this.chunkLoadingTime = start - System.nanoTime();
		}
		this.resetStates();
	}

	private void unloadAndCheckVisibilityOfChunks()
	{
		int   centerX            = (int) this.chunkManager.player().camera().position().x / Terrain.WIDTH;
		int   centerZ            = (int) this.chunkManager.player().camera().position().z / Terrain.DEPTH;
		int   renderDistance     = (int) this.chunkManager.renderDistance().current();
		float maxDistanceSquared = (renderDistance * Terrain.WIDTH) * (renderDistance * Terrain.WIDTH);

		synchronized (this.chunkManager.chunks())
		{
			for (var chunk : this.chunkManager.chunks().values())
			{
				float dx = centerX - (chunk.terrain().x() / Terrain.WIDTH);
				float dz = centerZ - (chunk.terrain().z() / Terrain.DEPTH);
				float distanceSquared =
						dx * dx * Terrain.WIDTH * Terrain.WIDTH + dz * dz * Terrain.DEPTH * Terrain.DEPTH;

				if (this.needCheckLoad && distanceSquared > maxDistanceSquared)
				{
					this.chunkManager.updateVisibility(chunk, false);
					chunk.needUnload(true);
					chunk.cleanup();
					this.chunkManager.chunks().remove(chunk.key());
					for (var listener : this.chunkManager.unloadedListeners())
					{
						listener.onChunk(chunk);
					}
					LOGGER.info(String.format("Unloaded chunk at (%.2f, %.2f) with key %s", dx, dz, chunk.key()));
				}
				else if (this.needCheckVisibility)
				{
					this.chunkManager.updateVisibility(chunk);
				}
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

					synchronized (this.chunkLoadingQueue)
					{
						this.chunkLoadingQueue.add(new ChunkManager.ChunkLoading(x, z, () ->
						{
							LOGGER.info(String.format("Loading chunk at %s", key));
							return this.chunkManager.loadChunk(key, finalX, finalZ, distanceSquared);
						}, key));
					}
				}
			}
		}
	}

	public final void resetStates()
	{
		this.chunkLoadingTime    = 0L;
		this.needCheckLoad       = false;
		this.needCheckVisibility = false;
		this.needCheckUnload     = false;
	}

	public final void updateStates()
	{
		this.resetStates();

		if (!this.chunkManager.player().updateState().hasChanged())
		{
			return;
		}

		this.chunkManager.frustumIntersection().set(this.chunkManager.player().camera().projectionViewMatrix());

		this.needCheckLoad       = this.chunkManager.player().updateState().hasMoveOneBlock();
		this.needCheckVisibility = true;
		this.needCheckUnload     = !this.chunkManager.player().updateState().hasMoveOneChunk();
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
