package fr.yukina.game.world.chunk.loader;

import fr.yukina.game.utils.thread.OrderingMachine;
import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.IChunk;
import fr.yukina.game.world.chunk.loader.pattern.Circle;
import fr.yukina.game.world.chunk.loader.pattern.EmptyCircle;
import fr.yukina.game.world.chunk.loader.pattern.PatternManager;
import fr.yukina.game.world.terrain.Terrain;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.logging.Logger;

public class ChunkLoaderThreaded implements IChunkLoader
{
	private static final Logger LOGGER = Logger.getLogger(ChunkLoaderThreaded.class.getName());

	private final ChunkManager                                               chunkManager;
	private final OrderingMachine<String, ChunkManager.ChunkLoading, IChunk> orderingMachine;
	private final PatternManager                                             patternManager;
	private final EmptyCircle                                                pattern;

	private long chunkLoadingTime = 0L;

	private          boolean needCheckLoad;
	private          boolean needCheckUnload;
	private volatile boolean firstExecution = true;

	private volatile boolean loadFull = true;
	private          float   lastPlayerX;
	private          float   lastPlayerZ;
	private          int     playerChunkMovementDistance;
	private          float   renderDistanceVariation;
	private          float   lastRenderDistance;
	private          float   lastDepth;

	public ChunkLoaderThreaded(ChunkManager chunkManagerIn)
	{
		this.chunkManager    = chunkManagerIn;
		this.orderingMachine = new OrderingMachine<String, ChunkManager.ChunkLoading, IChunk>(
				(queueIn) -> this.updateStatesAndLoadQueue(), (valueIn) -> valueIn.loadFunction().get(),
				(valueIn) -> this.chunkManager.key(valueIn.x(), valueIn.z()),
				Runtime.getRuntime().availableProcessors(), new PriorityQueue<>(Comparator.comparingInt(
				chunkLoading -> chunkManager.isOutFrustum(chunkLoading.x(), chunkLoading.z()) ? 1 : -1)));
		this.orderingMachine.maxSubmitCount(
				(int) (this.chunkManager.renderDistance().current() * 2 * this.chunkManager.renderDistance().current()
				       * 2));
		this.orderingMachine.orderingInterval(40L);
		this.patternManager = new PatternManager();
		this.pattern        = new EmptyCircle(this.chunkManager.renderDistance().current(),
		                                      this.chunkManager.renderDistance().current(), Terrain.WIDTH,
		                                      Terrain.DEPTH);
		this.patternManager.set(
				new Circle(this.chunkManager.renderDistance().current(), this.chunkManager.renderDistance().current(),
				           Terrain.WIDTH, Terrain.DEPTH));
	}

	public final void update()
	{
		if (!this.orderingMachine.isRunning())
		{
			this.orderingMachine.start();
		}

		checkVisibility();
	}

	private void updateStatesAndLoadQueue()
	{
		this.updateStates();

		if (this.needCheckUnload)
		{
			checkUnload();
		}

		if (this.needCheckLoad)
		{
			this.orderingMachine.stopTasks();
			this.patternManager.forEach((xIn, zIn) -> this.checkChunkLoad(xIn, zIn));

			if (this.loadFull)
			{
				this.loadFull = false;
			}
			if (this.firstExecution)
			{
				this.firstExecution = false;
			}
			this.resetStates();
		}
	}

	private void checkVisibility()
	{
		synchronized (this.chunkManager.frustumIntersection())
		{
			for (var chunk : this.chunkManager.chunks().values())
			{
				chunk.visible(!this.chunkManager.isOutFrustum(chunk));
			}
		}
	}

	private void checkUnload()
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

		if (this.chunkManager.chunks().containsKey(key))
		{
			return;
		}
		this.orderingMachine.waitingQueue().add(new ChunkManager.ChunkLoading(x, z, () ->
		{
			LOGGER.info(String.format("Loading chunk at " + "%s", key));
			return this.chunkManager.loadChunk(key, x, z);
		}, key));
	}

	private void loadEmptyCircle(int radiusIn, double depthIn, Queue<ChunkManager.ChunkLoading> queueIn)
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

	public final void resetStates()
	{
		this.chunkLoadingTime = 0L;
		this.needCheckLoad    = false;
		this.needCheckUnload  = false;
	}

	public final void updateStates()
	{
		var renderDistanceDiff = 0.0f;
		synchronized (this.chunkManager.renderDistance())
		{
			renderDistanceDiff = this.chunkManager.renderDistance().current() - this.lastRenderDistance;
		}

		synchronized (this.chunkManager.player())
		{
			if (!this.chunkManager.player().updateState().hasChanged() && renderDistanceDiff == 0
			    && !this.firstExecution)
			{
				return;
			}
		}

		if (renderDistanceDiff != 0)
		{
			synchronized (this.chunkManager.renderDistance())
			{
				this.renderDistanceVariation = renderDistanceDiff;
				this.lastRenderDistance      = this.chunkManager.renderDistance().current();
			}
		}

		synchronized (this.chunkManager.player())
		{
			synchronized (this.chunkManager.renderDistance())
			{
				double distX = (this.chunkManager.player().camera().position().x() - this.lastPlayerX) / Terrain.WIDTH;
				double distZ = (this.chunkManager.player().camera().position().z() - this.lastPlayerZ) / Terrain.DEPTH;
				this.playerChunkMovementDistance = (int) Math.sqrt(distX * distX + distZ * distZ);
				lastPlayerX                      = this.chunkManager.player().camera().position().x();
				lastPlayerZ                      = this.chunkManager.player().camera().position().z();
			}

			synchronized (this.chunkManager.frustumIntersection())
			{
				this.chunkManager.frustumIntersection().set(this.chunkManager.player().camera().projectionViewMatrix());
			}

			this.needCheckLoad   =
					this.chunkManager.player().updateState().hasMoveOneChunk() || this.chunkManager.renderDistance()
					                                                                               .hasChanged()
					|| this.firstExecution;
			this.needCheckUnload =
					this.chunkManager.player().updateState().hasMoveOneChunk() || this.chunkManager.renderDistance()
					                                                                               .hasChanged();
		}

		if (this.firstExecution)
		{
			this.lastRenderDistance          = this.chunkManager.renderDistance().current();
			this.playerChunkMovementDistance = 0;
		}

		if (this.loadFull)
		{
		}
		else if (!this.firstExecution)
		{
			var depth = Math.min(1.0f, Math.max(this.playerChunkMovementDistance, this.renderDistanceVariation));
			if (depth != this.lastDepth)
			{
				this.lastDepth = depth;
				this.pattern.set(depth);
				this.patternManager.set(this.pattern);
			}
		}
	}

	public void cleanup()
	{
		this.orderingMachine.stop();
	}
}
