package fr.yukina.game.world.chunk.loader;

import fr.yukina.game.utils.thread.OrderingMachine;
import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.IChunk;
import fr.yukina.game.world.chunk.loader.pattern.Circle;
import fr.yukina.game.world.chunk.loader.pattern.PatternManager;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

public class ChunkLoaderThreaded implements IChunkLoader
{
	private static final Logger LOGGER = Logger.getLogger(ChunkLoaderThreaded.class.getName());

	private final         ChunkManager                                               chunkManager;
	private final @Getter OrderingMachine<String, ChunkManager.ChunkLoading, IChunk> orderingMachine;
	private final         PatternManager                                             patternManager;
	private final         Circle                                                     circlePattern;

	private volatile boolean needChunkLoading;
	private volatile boolean needChunkUnloading;
	private volatile boolean needCheckVisibility;

	private volatile AtomicBoolean frustumIntersectionHasChanged = new AtomicBoolean();

	public ChunkLoaderThreaded(ChunkManager chunkManagerIn)
	{
		this.chunkManager    = chunkManagerIn;
		this.orderingMachine = new OrderingMachine<String, ChunkManager.ChunkLoading, IChunk>(
				(queueIn) -> this.updateStatesAndLoadQueue(), (valueIn) -> valueIn.loadFunction().get(),
				(valueIn) -> this.chunkManager.key(valueIn.x(), valueIn.z()),
				Runtime.getRuntime().availableProcessors() / 2, Comparator.comparingInt(chunkLoading ->
				                                                                        {
					                                                                        if (chunkLoading == null)
					                                                                        {
						                                                                        return -4;
					                                                                        }

					                                                                        if (this.frustumIntersectionHasChanged.get())
					                                                                        {
						                                                                        chunkLoading.updateVisibility(
								                                                                        this.chunkManager);
					                                                                        }
					                                                                        this.frustumIntersectionHasChanged.set(
							                                                                        false);

					                                                                        return chunkLoading.visible()
					                                                                               ? 1
					                                                                               : -1;
				                                                                        }));
		this.orderingMachine.maxSubmitCount(256 * 2 * 256 * 2);
		this.orderingMachine.orderingInterval(40);
		this.patternManager = new PatternManager();
		this.circlePattern  = new Circle(this.chunkManager.renderDistance().current(),
		                                 this.chunkManager.renderDistance().current(), Terrain.WIDTH, Terrain.DEPTH);
		this.patternManager.set(this.circlePattern);
	}

	public final void update()
	{
		if (!this.orderingMachine.isRunning())
		{
			this.orderingMachine.start();
		}

		if (this.needCheckVisibility)
		{
			checkVisibility();
		}
	}

	private void updateStatesAndLoadQueue()
	{
		if (this.needChunkUnloading)
		{
			checkUnload();
		}

		if (this.needChunkLoading)
		{
			this.orderingMachine.stopTasks();
			this.patternManager.forEach((xIn, zIn) -> this.checkChunkLoad(xIn, zIn));
		}
	}

	private void checkVisibility()
	{
		synchronized (this.chunkManager.frustumIntersection())
		{
			for (var chunk : this.chunkManager.chunks().values())
			{
				this.chunkManager.updateVisibility(chunk);
			}
		}
	}

	private void checkUnload()
	{
		var centerX        = this.chunkManager.player().camera().position().x;
		var centerZ        = this.chunkManager.player().camera().position().z;
		var renderDistance = this.chunkManager.renderDistance().current();
		var maxDistanceSquared = (renderDistance * Terrain.WIDTH) * (renderDistance * Terrain.WIDTH)
		                         + (renderDistance * Terrain.DEPTH) * (renderDistance * Terrain.DEPTH);

		synchronized (this.chunkManager.chunks())
		{
			for (var chunk : this.chunkManager.chunks().values())
			{
				float dx              = centerX - chunk.terrain().x();
				float dz              = centerZ - chunk.terrain().z();
				float distanceSquared = dx * dx + dz * dz;

				if (distanceSquared > maxDistanceSquared)
				{
					this.chunkManager.updateVisibility(chunk, false);
					chunk.needUnload(true);
					chunk.cleanup();
					this.chunkManager.chunks().remove(chunk.key());
					this.orderingMachine.remove(chunk.key());
					for (var listener : this.chunkManager.unloadedListeners())
					{
						listener.onChunk(chunk);
					}
				}
			}
		}
	}

	public final void checkChunkLoad(int xIn, int zIn)
	{
		final int x;
		final int z;
		synchronized (this.chunkManager.player())
		{
			x = (int) (xIn + this.chunkManager.player().camera().position().x() / Terrain.WIDTH);
			z = (int) (zIn + this.chunkManager.player().camera().position().z() / Terrain.DEPTH);
		}

		synchronized (this.chunkManager.renderDistance())
		{
			if (this.chunkManager.renderDistance().isOut(x * Terrain.WIDTH, z * Terrain.DEPTH))
			{
				return;
			}
		}

		String key = this.chunkManager.key(x, z);

		synchronized (this.chunkManager.chunks())
		{
			if (this.chunkManager.chunks().containsKey(key))
			{
				return;
			}
		}

		this.orderingMachine.submit(key, new ChunkManager.ChunkLoading(x, z, () ->
		{
			return this.chunkManager.loadChunk(key, x, z,
			                                   this.chunkManager.renderDistance().distance(x * Terrain.WIDTH,
			                                                                                          z
			                                                                                          * Terrain.DEPTH));
		}, key));
	}

	/*private void loadEmptyCircle(int radiusIn, double depthIn, Queue<ChunkManager.ChunkLoading> queueIn)
	{
		int   centerX            = (int) this.chunkManager.player().camera().position().x / Terrain.WIDTH;
		int   centerZ            = (int) this.chunkManager.player().camera().position().z / Terrain.DEPTH;
		int   renderDistance     = (int) this.chunkManager.renderDistance().current();
		float maxDistanceSquared = (renderDistance * Terrain.WIDTH) * (renderDistance * Terrain.WIDTH);

		var minChunkSize = Math.min(Terrain.WIDTH, Terrain.DEPTH);
		var maxChunkSize = Math.sqrt(Terrain.WIDTH * Terrain.WIDTH + Terrain.DEPTH * Terrain.DEPTH);
		for (int r = (int) (radiusIn - depthIn - 1); r <= radiusIn; r++)
		{
			double perimeter      = 2 * (r * maxChunkSize) * Math.PI;
			int    numPoints      = (int) Math.ceil(perimeter / (minChunkSize - 1));
			double angleIncrement = 360.0D / numPoints;

			for (int i = 0; i < numPoints; i++)
			{
				double angle   = i * angleIncrement;
				double radians = Math.toRadians(angle);
				int    x       = centerX + (int) (r * Math.cos(radians));
				int    z       = centerZ + (int) (r * Math.sin(radians));

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

				synchronized (this.chunkManager.chunks())
				{
					if (!this.chunkManager.chunks().containsKey(key))
					{
						queueIn.add(new ChunkManager.ChunkLoading(x, z, () ->
						{
							LOGGER.info(String.format("Loading chunk at %s", key));
							return this.chunkManager.loadChunk(key, x, z);
						}, key));
					}
				}
			}
		}
	}*/

	public synchronized final void frustumIntersectionHasChanged(boolean hasChangedIn)
	{
		this.frustumIntersectionHasChanged.set(hasChangedIn);
	}

	public synchronized final void needChunkLoading(boolean needChunkLoadingIn)
	{
		this.needChunkLoading = needChunkLoadingIn;
	}

	public synchronized final void needChunkUnloading(boolean needChunkUnloadingIn)
	{
		this.needChunkUnloading = needChunkUnloadingIn;
	}

	public synchronized final void setNeedCheckVisibility(boolean needCheckVisibilityIn)
	{
		this.needCheckVisibility = needCheckVisibilityIn;
	}

	public final void updateRenderDistance(int renderDistanceIn)
	{
		this.circlePattern.updateRadius(renderDistanceIn, renderDistanceIn);
		this.patternManager.set(this.circlePattern);
	}

	public void cleanup()
	{
		this.orderingMachine.stop();
	}
}
