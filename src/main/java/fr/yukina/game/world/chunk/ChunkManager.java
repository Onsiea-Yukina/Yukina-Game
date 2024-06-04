package fr.yukina.game.world.chunk;

import fr.yukina.game.Player;
import fr.yukina.game.world.terrain.ITerrainGenerator;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import org.joml.FrustumIntersection;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

@Getter
public class ChunkManager
{
	private final int               renderDistance;
	private final double            renderDistanceBlockX;
	private final double            renderDistanceBlockZ;
	private final double            renderDistanceBlock;
	private final ITerrainGenerator generator;

	private final List<IChunkListener> loadedListeners;
	private final List<IChunkListener> unloadedListeners;

	private final ConcurrentMap<String, Chunk> chunks;
	private final Player                       player;
	private final FrustumIntersection          frustumIntersection;
	private final IChunkLoader                 chunkLoader;

	public ChunkManager(int renderDistanceIn, ITerrainGenerator generatorIn, Player playerIn)
	{
		this.renderDistance       = renderDistanceIn;
		this.generator            = generatorIn;
		this.loadedListeners      = new CopyOnWriteArrayList<>();
		this.unloadedListeners    = new CopyOnWriteArrayList<>();
		this.chunks               = new ConcurrentHashMap<>();
		this.player               = playerIn;
		this.renderDistanceBlockX = (this.renderDistance * Terrain.WIDTH);
		this.renderDistanceBlockZ = (this.renderDistance * Terrain.DEPTH);
		this.renderDistanceBlock  = this.renderDistanceBlockX * this.renderDistanceBlockX
		                            + this.renderDistanceBlockZ * this.renderDistanceBlockZ;
		this.frustumIntersection  = new FrustumIntersection();
		chunkLoader               = new ChunkLoader(this);
	}

	public final void update()
	{
		this.chunkLoader.update();
	}

	public boolean isOutFrustum(Chunk chunkIn)
	{
		return !this.frustumIntersection.testAab(chunkIn.min(), chunkIn.max());
	}

	public boolean isOutFrustum(int xIn, int zIn)
	{
		return !this.frustumIntersection.testAab(xIn * Terrain.WIDTH, 0, zIn * Terrain.DEPTH,
		                                         xIn * Terrain.WIDTH + Terrain.WIDTH, 0,
		                                         zIn * Terrain.DEPTH + Terrain.DEPTH);
	}

	boolean isOutRenderDistance(Chunk chunkIn)
	{
		return isOutRenderDistance(chunkIn.terrain().x(), chunkIn.terrain().z());
	}

	boolean isOutRenderDistance(float xIn, float zIn)
	{
		double centerX  = this.player.camera().position().x;
		double centerZ  = this.player.camera().position().z;
		double distX    = centerX - xIn;
		double distZ    = centerZ - zIn;
		double distance = distX * distX + distZ * distZ;
		return distance > this.renderDistanceBlock;
	}

	Chunk loadChunk(String keyIn, int xIn, int zIn)
	{
		var chunk = new Chunk(keyIn, xIn * Terrain.WIDTH, 0, zIn * Terrain.DEPTH, Terrain.WIDTH, Terrain.DEPTH,
		                      this.generator());

		chunk.generate();
		this.chunks().put(chunk.key(), chunk);
		for (var listener : this.loadedListeners())
		{
			listener.onChunk(chunk);
		}

		return chunk;
	}

	public void cleanup()
	{
		this.chunks.clear();
		this.loadedListeners.clear();
		this.unloadedListeners.clear();
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

	@Getter
	public final static class ChunkLoading
	{
		private final int             x;
		private final int             z;
		private final Supplier<Chunk> loadFunction;

		public ChunkLoading(int xIn, int zIn, Supplier<Chunk> loadFunctionIn)
		{
			this.x            = xIn;
			this.z            = zIn;
			this.loadFunction = loadFunctionIn;
		}
	}
}