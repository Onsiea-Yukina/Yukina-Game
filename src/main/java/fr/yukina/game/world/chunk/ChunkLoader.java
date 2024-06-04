package fr.yukina.game.world.chunk;

import fr.yukina.game.world.terrain.Terrain;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChunkLoader implements IChunkLoader
{
	private final ChunkManager                     chunkManager;
	private final Queue<ChunkManager.ChunkLoading> unpriorizedChunkLoadingQueue;
	private       long                             chunkLoadingTime = 0L;
	private       boolean                          needCheckLoad;
	private       boolean                          needCheckVisibility;
	private       boolean                          needCheckLoadPriority;
	private       boolean                          needCheckUnload;

	public ChunkLoader(ChunkManager chunkManagerIn)
	{
		this.chunkManager                 = chunkManagerIn;
		this.unpriorizedChunkLoadingQueue = new ConcurrentLinkedQueue<>();
	}

	public final void update()
	{
		this.updateStates();

		var start = System.nanoTime();
		// Check unload and visibility of chunks
		if (this.needCheckUnload || this.needCheckVisibility)
		{
			for (var chunk : this.chunkManager.chunks().values())
			{
				if (this.needCheckLoad && this.chunkManager.isOutRenderDistance(chunk))
				{
					chunk.visible(false);
					chunk.needUnload(true);
					chunk.cleanup();
					this.chunkManager.chunks().remove(chunk.key());
					for (var listener : this.chunkManager.unloadedListeners())
					{
						listener.onChunk(chunk);
					}
				}
				else if (this.needCheckVisibility)
				{
					chunk.visible(!this.chunkManager.isOutFrustum(chunk));
				}
			}
		}

		if (this.needCheckLoad)
		{
			this.unpriorizedChunkLoadingQueue.clear();

			// Load chunks
			int centerX = (int) this.chunkManager.player().camera().position().x / Terrain.WIDTH;
			int centerZ = (int) this.chunkManager.player().camera().position().z / Terrain.DEPTH;
			int width   = this.chunkManager.renderDistance();
			int depth   = this.chunkManager.renderDistance();

			for (int x = (int) (centerX - (width * 1.5f)); x < (int) (centerX + (width * 1.5f)); x++)
			{
				for (int z = (int) (centerZ - (depth * 1.5f)); z < (int) (centerZ + (depth * 1.5f)); z++)
				{
					if (this.chunkManager.isOutRenderDistance(x * Terrain.WIDTH, z * Terrain.DEPTH))
					{
						continue;
					}

					String key = this.chunkManager.key(x, z);

					if (!this.chunkManager.chunks().containsKey(key))
					{
						if (this.chunkManager.isOutFrustum(x, z))
						{
							final int finalX = x;
							final int finalZ = z;
							this.unpriorizedChunkLoadingQueue.add(new ChunkManager.ChunkLoading(x, z,
							                                                                    () -> this.chunkManager.loadChunk(
									                                                                    key, finalX,
									                                                                    finalZ)));
							continue;
						}

						this.chunkManager.loadChunk(key, x, z);
					}
				}
			}
		}
		else if (this.needCheckLoadPriority)
		{
			var iterator = this.unpriorizedChunkLoadingQueue.iterator();

			while (iterator.hasNext())
			{
				var chunkLoading = iterator.next();
				if (chunkLoading == null || this.chunkManager.isOutFrustum(chunkLoading.x(), chunkLoading.z()))
				{
					continue;
				}
				iterator.remove();
				chunkLoading.loadFunction().get();
			}
		}
		this.chunkLoadingTime = System.nanoTime() - start;

		while (!this.unpriorizedChunkLoadingQueue.isEmpty() && this.chunkLoadingTime < 16_000_000L / 2)
		{
			start = System.nanoTime();
			var chunkLoading = this.unpriorizedChunkLoadingQueue.poll();
			if (chunkLoading != null)
			{
				chunkLoading.loadFunction().get();
			}
			this.chunkLoadingTime += System.nanoTime() - start;
		}
		this.resetStates();
	}

	public final void resetStates()
	{
		this.chunkLoadingTime      = 0L;
		this.needCheckLoad         = false;
		this.needCheckVisibility   = false;
		this.needCheckLoadPriority = false;
		this.needCheckUnload       = false;
	}

	public final void updateStates()
	{
		this.resetStates();

		if (!this.chunkManager.player().updateState().hasChanged())
		{
			return;
		}

		this.chunkManager.frustumIntersection().set(this.chunkManager.player().camera().projectionViewMatrix());

		this.needCheckLoad         = this.chunkManager.player().updateState().hasMoveOneBlock();
		this.needCheckVisibility   = true;
		this.needCheckLoadPriority = !this.chunkManager.player().updateState().hasMoveOneChunk();
		this.needCheckUnload       = !this.chunkManager.player().updateState().hasMoveOneChunk();
	}
}
