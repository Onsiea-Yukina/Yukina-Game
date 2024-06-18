package fr.yukina.game.world.chunk;

import fr.yukina.game.Player;
import fr.yukina.game.world.RenderDistance;
import fr.yukina.game.world.chunk.loader.ChunkLoaderThreaded;
import fr.yukina.game.world.terrain.ITerrainGenerator;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import org.joml.FrustumIntersection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Getter
public class ChunkManager
{
	private static final Logger            LOGGER = Logger.getLogger(ChunkManager.class.getName());
	private final        RenderDistance    renderDistance;
	private final        boolean           needValidation;
	private              boolean           firstExecution;
	private final        ITerrainGenerator generator;

	private final List<IChunkListener> loadedListeners;
	private final List<IChunkListener> infoListeners;
	private final List<IChunkListener> unloadedListeners;

	private final Map<String, IChunk> chunks;
	private final Player              player;
	private final FrustumIntersection frustumIntersection;
	private final ChunkLoaderThreaded chunkLoader;
	private       byte                updateFrustumFrame;

	public ChunkManager(int renderDistanceIn, ITerrainGenerator generatorIn, Player playerIn)
	{
		this.renderDistance      = new RenderDistance(renderDistanceIn, 500_000_000L, 8_500_000_000L, 37, 2, 1.75f,
		                                              playerIn);
		this.generator           = generatorIn;
		this.loadedListeners     = new CopyOnWriteArrayList<>();
		this.infoListeners       = new CopyOnWriteArrayList<>();
		this.unloadedListeners   = new CopyOnWriteArrayList<>();
		this.chunks              = new ConcurrentHashMap<>();
		this.player              = playerIn;
		this.frustumIntersection = new FrustumIntersection();
		this.chunkLoader         = new ChunkLoaderThreaded(this);
		this.needValidation      = false;
		this.firstExecution      = true;
	}

	public final void update()
	{
		this.renderDistance.update(this.chunkLoader.orderingMachine().waitingQueue().size() <= 25);

		if (this.player.updateState().hasChanged())
		{
			this.frustumIntersection.set(this.player().camera().projectionViewMatrix());
			this.updateFrustumFrame++;
			this.chunkLoader.frustumIntersectionHasChanged(true);
			var need = this.player.updateState().hasMoveOneChunk() || this.firstExecution;
			this.chunkLoader.needChunkLoading(need);
			this.chunkLoader.needChunkUnloading(need);
			this.chunkLoader.setNeedCheckVisibility(this.player.updateState().hasRotated());
		}
		else
		{
			this.chunkLoader.needChunkLoading(this.firstExecution);
			this.chunkLoader.needChunkUnloading(this.firstExecution);
			this.chunkLoader.setNeedCheckVisibility(false);
			this.chunkLoader.frustumIntersectionHasChanged(false);
		}

		if (this.renderDistance.hasChanged())
		{
			this.chunkLoader.needChunkLoading(true);
			this.chunkLoader.needChunkUnloading(true);
			this.chunkLoader.updateRenderDistance((int) this.renderDistance.current());
		}

		this.chunkLoader.update();

		if (this.needValidation)
		{
			validateChunks();

			var centerX = this.player.camera().position().x;
			var centerZ = this.player.camera().position().z;
			var chunk   = this.chunks.get(key((int) centerX / Terrain.WIDTH, (int) centerZ / Terrain.DEPTH));
			if (chunk != null)
			{
				System.out.println(chunk.key() + " at " + chunk.terrain().x() + ", " + chunk.terrain().z());
				System.out.println("{");
				System.out.println("  x: " + chunk.terrain().x());
				System.out.println("  y: " + chunk.terrain().y());
				System.out.println("  z: " + chunk.terrain().z());
				System.out.println("  width: " + chunk.terrain().width());
				System.out.println("  depth: " + chunk.terrain().depth());
				System.out.println("  visible: " + chunk.visible());
				System.out.println("  need unload: " + chunk.needUnload());
				for (var listener : this.infoListeners)
				{
					listener.onChunk(chunk);
				}
				System.out.println("}");
			}
		}
	}

	public boolean isOutFrustum(IChunk chunkIn)
	{
		return !this.frustumIntersection.testAab(chunkIn.min(), chunkIn.max());
	}

	public boolean isOutFrustum(int xIn, int zIn)
	{
		return !this.frustumIntersection.testAab(xIn * Terrain.WIDTH, 0, zIn * Terrain.DEPTH,
		                                         xIn * Terrain.WIDTH + Terrain.WIDTH, 0,
		                                         zIn * Terrain.DEPTH + Terrain.DEPTH);
	}

	public final void updateVisibility(IChunk chunkIn)
	{
		if (chunkIn.frustumUpdateFrame() != this.updateFrustumFrame)
		{
			chunkIn.visible(this.updateFrustumFrame, !this.isOutFrustum(chunkIn));
		}
	}

	public final void updateVisibility(IChunk chunkIn, boolean visibilityIn)
	{
		chunkIn.visible(this.updateFrustumFrame, visibilityIn);
	}

	public IChunk loadChunk(String keyIn, int xIn, int zIn, float distanceSquaredIn)
	{
		if (this.chunks.containsKey(keyIn))
		{
			System.out.println("Chunk already loaded: " + keyIn);
			return this.chunks.get(keyIn);
		}

		var maxDistanceSquared = ((renderDistance.max()) * Terrain.WIDTH) * ((renderDistance.max()) * Terrain.WIDTH)
		                         + ((renderDistance.max()) * Terrain.DEPTH) * ((renderDistance.max()) * Terrain.DEPTH);
		var lod = 1.0f;
		if (distanceSquaredIn > maxDistanceSquared * 0.90f)
		{
			lod = 32.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.40f)
		{
			lod = 24.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.20f)
		{
			lod = 20.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.15f)
		{
			lod = 16.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.10f)
		{
			lod = 14.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.5f)
		{
			lod = 12.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.05f)
		{
			lod = 10.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.005f)
		{
			lod = 8.0f;
		}
		else if (distanceSquaredIn > maxDistanceSquared * 0.0025f)
		{
			lod = 4.0f;
		}

		var chunk = new ChunkLink(keyIn, xIn * Terrain.WIDTH, 0, zIn * Terrain.DEPTH, Terrain.WIDTH, Terrain.DEPTH,
		                          this.generator, lod);

		chunk.generate();
		this.chunks().put(chunk.key(), chunk);
		for (var listener : this.loadedListeners())
		{
			listener.onChunk(chunk);
		}

		LOGGER.warning(
				String.format("Loaded chunk at (%d, %d) with key %s [%s] " + this.chunks.containsKey(keyIn), xIn, zIn,
				              keyIn, chunk.key()));

		return chunk;
	}

	public void cleanup()
	{
		this.chunkLoader.cleanup();
		this.chunks.clear();
		this.loadedListeners.clear();
		this.unloadedListeners.clear();
	}

	public String key(int xIn, int zIn)
	{
		String key = xIn + ":" + zIn;
		LOGGER.info(String.format("Generated key for chunk at (%d, %d): %s", xIn, zIn, key));
		return key;
	}

	public void addLoadedListener(IChunkListener listenerIn)
	{
		loadedListeners.add(listenerIn);
	}

	public void addInfoListener(IChunkListener listenerIn)
	{
		infoListeners.add(listenerIn);
	}

	public void addUnloadedListener(IChunkListener listenerIn)
	{
		unloadedListeners.add(listenerIn);
	}

	private void validateChunks()
	{
		chunks.forEach((key, chunk) ->
		               {
			               boolean outOfRenderDistance = this.renderDistance.isOut(chunk);
			               boolean outOfFrustum        = isOutFrustum(chunk);
			               if (outOfRenderDistance)
			               {
				               LOGGER.warning(
						               String.format("Chunk %s is out of render distance but still loaded", key));
			               }
			               if (outOfFrustum && chunk.visible())
			               {
				               LOGGER.warning(String.format("Chunk %s is out of frustum but marked as visible", key));
			               }
		               });
	}

	public interface IChunkListener
	{
		void onChunk(IChunk chunkIn);
	}

	@Getter
	public final static class ChunkLoading
	{
		private final int              x;
		private final int              z;
		private       int              frustumUpdateFrame;
		private       boolean          visible;
		private final Supplier<IChunk> loadFunction;
		private final String           key;

		public ChunkLoading(int xIn, int zIn, Supplier<IChunk> loadFunctionIn, String keyIn)
		{
			this.x            = xIn;
			this.z            = zIn;
			this.loadFunction = loadFunctionIn;
			this.key          = keyIn;
		}

		public ChunkLoading updateVisibility(ChunkManager chunkManagerIn)
		{
			if (this.frustumUpdateFrame == chunkManagerIn.updateFrustumFrame())
			{
				return this;
			}

			this.frustumUpdateFrame = chunkManagerIn.updateFrustumFrame();
			this.visible            = chunkManagerIn.isOutFrustum(this.x, this.z);

			return this;
		}

		public ChunkLoading visible(int frustumUpdateFrameIn, boolean visibleIn)
		{
			this.frustumUpdateFrame = frustumUpdateFrameIn;
			this.visible            = visibleIn;

			return this;
		}
	}
}