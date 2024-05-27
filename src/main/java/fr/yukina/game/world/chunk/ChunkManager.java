package fr.yukina.game.world.chunk;

import fr.yukina.game.Player;
import fr.yukina.game.world.terrain.ITerrainGenerator;
import fr.yukina.game.world.terrain.Terrain;

import java.util.List;
import java.util.concurrent.*;

public class ChunkManager
{
	private final    int                          renderDistance;
	private final    double                       renderDistanceBlockX;
	private final    double                       renderDistanceBlockZ;
	private final    double                       renderDistanceBlock;
	private final    ITerrainGenerator            generator;
	private final    ExecutorService              chunkLoadingService;
	private final    ExecutorService              chunkUnloadingService;
	private final    ConcurrentLinkedQueue<Chunk> loadingChunks;
	private final    ConcurrentLinkedQueue<Chunk> unloadingChunks;
	private final    List<IChunkListener>         loadedListeners;
	private final    List<IChunkListener>         unloadedListeners;
	private final    Thread                       orderingChunksThread;
	private final    ConcurrentMap<String, Chunk> chunks;
	private final    Player                       player;
	private volatile boolean                      isRunning;
	private volatile boolean                      orderingThreadStopped;

	public ChunkManager(int renderDistanceIn, ITerrainGenerator generatorIn, Player playerIn)
	{
		this.renderDistance        = renderDistanceIn;
		this.generator             = generatorIn;
		this.chunkLoadingService   = Executors.newFixedThreadPool(8);
		this.chunkUnloadingService = Executors.newSingleThreadExecutor();
		this.loadingChunks         = new ConcurrentLinkedQueue<>();
		this.unloadingChunks       = new ConcurrentLinkedQueue<>();
		this.loadedListeners       = new CopyOnWriteArrayList<>();
		this.unloadedListeners     = new CopyOnWriteArrayList<>();
		this.chunks                = new ConcurrentHashMap<>();
		this.orderingChunksThread  = new Thread(this::orderingChunks);
		this.player                = playerIn;
		this.isRunning             = false;
		this.renderDistanceBlockX  = (this.renderDistance * Terrain.WIDTH) / 2.0D;
		this.renderDistanceBlockZ  = (this.renderDistance * Terrain.DEPTH) / 2.0D;
		this.renderDistanceBlock   = this.renderDistanceBlockX * this.renderDistanceBlockX
		                             + this.renderDistanceBlockZ * this.renderDistanceBlockZ;
	}

	public void start()
	{
		this.isRunning = true;
		this.orderingChunksThread.start();
	}

	public void stop()
	{
		this.isRunning = false;
		try
		{
			this.orderingChunksThread.join();
			var start = System.nanoTime();
			while (!this.orderingThreadStopped)
			{
				if (start - System.nanoTime() > 4_000_000_000L)
				{
					this.orderingChunksThread.interrupt();
					this.orderingThreadStopped = true;
					break;
				}
			}
		}
		catch (InterruptedException eIn)
		{
			throw new RuntimeException(eIn);
		}

		chunkLoadingService.shutdown();
		try
		{
			if (!chunkLoadingService.awaitTermination(4, TimeUnit.SECONDS))
			{
				chunkLoadingService.shutdownNow();
			}
		}
		catch (InterruptedException e)
		{
			chunkLoadingService.shutdownNow();
			Thread.currentThread().interrupt();
		}

		chunkUnloadingService.shutdown();
		try
		{
			if (!chunkUnloadingService.awaitTermination(4, TimeUnit.SECONDS))
			{
				chunkUnloadingService.shutdownNow();
			}
		}
		catch (InterruptedException e)
		{
			chunkUnloadingService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private void orderingChunks()
	{
		this.orderingThreadStopped = false;
		while (isRunning)
		{
			synchronized (player)
			{
				if (!player.updateState().hasChanged())
				{
					sleep(16);
					continue;
				}

				synchronized (chunks)
				{
					for (var chunk : chunks.values())
					{
						if (this.player.updateState().hasMove() || this.player.updateState().hasRotated())
						{
							boolean visible = true;
							if (isOutRenderDistance(chunk))
							{
								chunk.visible(false);
								chunk.needUnload(true);
								this.unloadingChunks.add(chunk);
								visible = false;
							}
							else if (isOutFrustum(chunk))
							{
								visible = false;
							}
							chunk.visible(visible);
						}
					}
				}

				this.loadingChunks.clear();
				surfaceLoop();
			}
			sleep(16);
		}
		this.orderingThreadStopped = true;
	}

	private void surfaceLoop()
	{
		int centerX = (int) this.player.camera().position().x / Terrain.WIDTH;
		int centerZ = (int) this.player.camera().position().z / Terrain.DEPTH;
		int width   = this.renderDistance;
		int depth   = this.renderDistance;

		for (int x = centerX - width; x <= centerX + width; x++)
		{
			for (int z = centerZ - depth; z <= centerZ + depth; z++)
			{
				if (isOutRenderDistance(x * Terrain.WIDTH, z * Terrain.DEPTH))
				{
					continue;
				}

				String key = key(x, z);
				synchronized (this.chunks)
				{
					if (!this.chunks.containsKey(key))
					{
						Chunk chunk = new Chunk(key, x * Terrain.WIDTH, 0, z * Terrain.DEPTH, Terrain.WIDTH,
						                        Terrain.DEPTH, generator);
						this.loadingChunks.add(chunk);
						CompletableFuture.runAsync(
								() -> new ChunkLoaderTask(chunk, this.chunks, this.loadedListeners).run(),
								chunkLoadingService);
					}
				}
			}
		}
	}

	private void sleep(int timeIn)
	{
		try
		{
			Thread.sleep(timeIn);
		}
		catch (InterruptedException eIn)
		{
			throw new RuntimeException(eIn);
		}
	}

	public boolean isOutFrustum(Chunk chunkIn)
	{
		return false;
	}

	private boolean isOutRenderDistance(Chunk chunkIn)
	{
		return isOutRenderDistance(chunkIn.terrain().x(), chunkIn.terrain().z());
	}

	private boolean isOutRenderDistance(float xIn, float zIn)
	{
		double centerX  = this.player.camera().position().x;
		double centerZ  = this.player.camera().position().z;
		double distX    = centerX - xIn;
		double distZ    = centerZ - zIn;
		double distance = distX * distX + distZ * distZ;
		return distance > this.renderDistanceBlock;
	}

	public void cleanup()
	{
		this.chunks.clear();
		this.loadedListeners.clear();
		this.unloadedListeners.clear();
		this.stop();
		this.loadingChunks.clear();
		this.unloadingChunks.clear();
	}

	public String key(int xIn, int zIn)
	{
		return xIn + ":" + zIn;
	}

	public void addLoadedListener(IChunkListener listenerIn)
	{
		loadedListeners.add(listenerIn);
	}

	public void addUnloadedListener(IChunkListener listenerIn)
	{
		unloadedListeners.add(listenerIn);
	}

	public interface IChunkListener
	{
		void onChunk(Chunk chunkIn);
	}
}