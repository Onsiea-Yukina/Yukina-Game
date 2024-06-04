package fr.yukina.game.render;

import fr.yukina.game.render.opengl.ShaderManager;
import fr.yukina.game.world.World;
import fr.yukina.game.world.chunk.Chunk;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Getter
public class WorldRenderer
{
	private final        World                                       world;
	private final        Map<String, ChunkRenderer>                  chunkRenderers;
	private final        ConcurrentLinkedQueue<ChunkRendererLoading> uploadQueue;
	private final        ConcurrentLinkedQueue<Chunk>                unloadQueue;
	private final        Queue<ChunkRendererLoading>                 preparedQueue;
	private final        ShaderManager                               shaderManager;
	private final        ExecutorService                             executorService;
	private volatile     boolean                                     hasChanged;
	private static final int                                         BATCH_SIZE = 5;

	public WorldRenderer(World worldIn)
	{
		this.world           = worldIn;
		this.chunkRenderers  = new ConcurrentHashMap<>();
		this.uploadQueue     = new ConcurrentLinkedQueue<>();
		this.unloadQueue     = new ConcurrentLinkedQueue<>();
		this.preparedQueue   = new ConcurrentLinkedQueue<>();
		this.shaderManager   = new ShaderManager();
		this.executorService = Executors.newFixedThreadPool(1);
		this.world.chunkManager().addLoadedListener(this::chunkLoaded);
		this.world.chunkManager().addUnloadedListener(this::chunkUnloaded);
	}

	public void initialize()
	{
		try
		{
			this.shaderManager.loadShaders("resources/shaders/terrain.vert", "resources/shaders/terrain.frag");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void update()
	{
		this.hasChanged = false;

		processPreparedChunks();

		processUnloadQueue();
	}

	private void processPreparedChunks()
	{
		int processed = 0;
		while (!preparedQueue.isEmpty() && processed < BATCH_SIZE)
		{
			var chunkRendererLoading = preparedQueue.poll();
			if (chunkRendererLoading != null)
			{
				var key = chunkRendererLoading.chunk().key();
				if (!chunkRenderers.containsKey(key))
				{
					var chunkRenderer = new ChunkRenderer(chunkRendererLoading.chunk());
					chunkRenderer.terrainRenderer()
					             .upload(chunkRendererLoading.vertices(), chunkRendererLoading.indices());
					chunkRenderers.put(key, chunkRenderer);
					hasChanged = true;
				}
				processed++;
			}
		}
	}

	private void processUnloadQueue()
	{
		int processed = 0;
		while (!unloadQueue.isEmpty() && processed < BATCH_SIZE)
		{
			var chunk = unloadQueue.poll();
			if (chunk != null)
			{
				var chunkRenderer = chunkRenderers.remove(chunk.key());
				if (chunkRenderer != null)
				{
					chunkRenderer.cleanup();
					hasChanged = true;
				}
				processed++;
			}
		}
	}

	public void chunkLoaded(Chunk chunkIn)
	{
		var chunkRendererLoading = new ChunkRendererLoading(chunkIn);
		uploadQueue.add(chunkRendererLoading);
		executorService.submit(() ->
		                       {
			                       chunkRendererLoading.prepare();
			                       preparedQueue.add(chunkRendererLoading);
		                       });
	}

	public void chunkUnloaded(Chunk chunkIn)
	{
		unloadQueue.add(chunkIn);
	}

	public void render(Matrix4f projectionMatrixIn, Matrix4f viewMatrixIn)
	{
		shaderManager.attach();
		synchronized (chunkRenderers)
		{
			for (ChunkRenderer chunkRenderer : chunkRenderers.values())
			{
				chunkRenderer.render();
			}
		}
		shaderManager.detach();
	}

	public void cleanup()
	{
		executorService.shutdown();
		try
		{
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS))
			{
				executorService.shutdownNow();
			}
		}
		catch (InterruptedException e)
		{
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
		chunkRenderers.clear();
		shaderManager.cleanup();
	}

	@Getter
	public final static class ChunkRendererLoading
	{
		private final Chunk       chunk;
		private final Terrain     terrain;
		private final FloatBuffer vertices;
		private final IntBuffer   indices;

		public ChunkRendererLoading(Chunk chunkIn)
		{
			this.chunk    = chunkIn;
			this.terrain  = chunkIn.terrain();
			this.vertices = BufferUtils.createFloatBuffer((this.terrain.width() + 1) * (this.terrain.depth() + 1) * 3);
			this.indices  = BufferUtils.createIntBuffer(this.terrain.depth() * this.terrain.depth() * 6);
		}

		public void prepare()
		{
			int width = this.terrain.width();
			int depth = this.terrain.depth();

			// Generate vertices
			for (int z = 0; z <= depth; z++)
			{
				for (int x = 0; x <= width; x++)
				{
					float height = this.terrain.height(x, z);
					this.vertices.put(this.terrain.x() + x);
					this.vertices.put(this.terrain.y() + height);
					this.vertices.put(this.terrain.z() + z);
				}
			}
			this.vertices.flip();

			// Generate indices
			for (int z = 0; z < depth; z++)
			{
				for (int x = 0; x < width; x++)
				{
					int topLeft     = z * (width + 1) + x;
					int topRight    = topLeft + 1;
					int bottomLeft  = topLeft + (width + 1);
					int bottomRight = bottomLeft + 1;

					this.indices.put(topLeft);
					this.indices.put(bottomLeft);
					this.indices.put(topRight);

					this.indices.put(topRight);
					this.indices.put(bottomLeft);
					this.indices.put(bottomRight);
				}
			}
			this.indices.flip();
		}
	}
}