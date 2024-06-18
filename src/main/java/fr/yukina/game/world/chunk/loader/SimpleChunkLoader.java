package fr.yukina.game.world.chunk.loader;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.terrain.Terrain;

import java.util.logging.Logger;

public class SimpleChunkLoader implements IChunkLoader
{
	private static final Logger LOGGER = Logger.getLogger(SimpleChunkLoader.class.getName());

	private final ChunkManager chunkManager;

	public SimpleChunkLoader(ChunkManager chunkManagerIn)
	{
		this.chunkManager = chunkManagerIn;
	}

	public final void update()
	{
		this.updateStates();

		// Check unload and visibility of chunks
		unloadAndCheckVisibilityOfChunks();

		// Load chunks
		loadChunksInCircularPattern();

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
			else
			{
				this.chunkManager.updateVisibility(chunk);
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

					LOGGER.info(String.format("Loading chunk at %s", key));
					this.chunkManager.loadChunk(key, finalX, finalZ, distanceSquared);
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
}
