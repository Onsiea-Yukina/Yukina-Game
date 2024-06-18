package fr.yukina.game.render;

import fr.yukina.game.render.opengl.OpenGLValidator;
import fr.yukina.game.render.opengl.ShaderManager;
import fr.yukina.game.world.World;
import fr.yukina.game.world.chunk.IChunk;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

@Getter
public class WorldRenderer
{
	private static final Logger LOGGER = Logger.getLogger(WorldRenderer.class.getName());

	private final        World                                    world;
	private final        Map<String, ChunkRenderer>               chunkRenderers;
	private final        ConcurrentLinkedQueue<IChunk>            unloadQueue;
	private final        Queue<ChunkRendererLoading>              preparedQueue;
	private final        ShaderManager                            shaderManager;
	private final        Map<String, AtomicReference<ChunkState>> chunkStates;
	private volatile     boolean                                  hasChanged;
	private static final int                                      BATCH_SIZE = 40;

	public WorldRenderer(World worldIn)
	{
		this.world          = worldIn;
		this.chunkRenderers = new ConcurrentHashMap<>();
		this.unloadQueue    = new ConcurrentLinkedQueue<>();
		this.preparedQueue  = new ConcurrentLinkedQueue<>();
		this.shaderManager  = new ShaderManager();
		this.chunkStates    = new ConcurrentHashMap<>();
		this.world.chunkManager().addLoadedListener(this::chunkLoaded);
		this.world.chunkManager().addInfoListener(this::chunkInfo);
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
		synchronized (preparedQueue)
		{
			int processed = 0;
			while (!preparedQueue.isEmpty() && processed < BATCH_SIZE)
			{
				var chunkRendererLoading = preparedQueue.poll();
				if (chunkRendererLoading != null)
				{
					var key = chunkRendererLoading.chunk().key();
					synchronized (chunkStates)
					{
						AtomicReference<ChunkState> stateRef = chunkStates.get(key);
						if (stateRef != null && stateRef.get() == ChunkState.UNLOADING)
						{
							continue;
						}
					}
					synchronized (chunkRenderers)
					{
						if (!chunkRenderers.containsKey(key))
						{
							var chunkRenderer = new ChunkRenderer(chunkRendererLoading.chunk());
							chunkRenderer.terrainRenderer()
							             .upload(chunkRendererLoading.vertices(), chunkRendererLoading.indices());
							chunkRenderers.put(key, chunkRenderer);
							hasChanged = true;
						}
					}
					processed++;
					synchronized (chunkStates)
					{
						chunkStates.remove(key);
					}
				}
			}
		}
	}

	private void processUnloadQueue()
	{
		int processed = 0;
		synchronized (unloadQueue)
		{
			while (!unloadQueue.isEmpty() && processed < BATCH_SIZE)
			{
				var chunk = unloadQueue.poll();
				if (chunk != null)
				{
					var key = chunk.key();
					synchronized (chunkStates)
					{
						AtomicReference<ChunkState> stateRef = chunkStates.get(key);
						if (stateRef != null && stateRef.get() == ChunkState.LOADING)
						{
							continue;
						}
					}

					synchronized (chunkRenderers)
					{
						var chunkRenderer = chunkRenderers.remove(key);
						if (chunkRenderer != null)
						{
							chunkRenderer.cleanup();
							hasChanged = true;
						}
					}

					processed++;

					synchronized (chunkStates)
					{
						chunkStates.remove(key);
					}
				}
			}
		}
	}

	private final Map<String, IChunk> chunks = new HashMap<>();

	public void chunkLoaded(IChunk chunkIn)
	{
		if (this.chunks.containsKey(chunkIn.key()))
		{
			return;
		}
		chunks.put(chunkIn.key(), chunkIn);
		var key = chunkIn.key();
		synchronized (chunkStates)
		{
			var state = chunkStates.putIfAbsent(key, new AtomicReference<>(ChunkState.LOADING));
			if (state != null && state.equals(ChunkState.UNLOADING))
			{
				return;
			}
		}
		synchronized (preparedQueue)
		{
			var chunkRendererLoading = new ChunkRendererLoading(chunkIn);
			chunkRendererLoading.prepare();
			preparedQueue.add(chunkRendererLoading);
		}
	}

	public void chunkInfo(IChunk chunkIn)
	{
		if (!this.world.chunkManager().needValidation())
		{
			return;
		}
		var        key   = chunkIn.key();
		ChunkState state = null;
		synchronized (chunkStates)
		{
			var atomic = chunkStates.get(key);
			if (atomic != null)
			{
				state = atomic.get();
			}
		}
		System.out.println("  state: " + (state == null ? "no state" : state));
		var chunkRenderer = chunkRenderers.get(key);
		if (chunkRenderer == null)
		{
			System.out.println("  is not present ");
			return;
		}
		System.out.println("  associated chunk: " + chunkRenderer.chunk().key());
		if (!key.equals(chunkRenderer.chunk().key()))
		{
			throw new RuntimeException("    keys are not equal");
		}
		if (chunkIn.visible() != chunkRenderer.chunk().visible())
		{
			throw new RuntimeException(
					"  visible: " + chunkIn.visible() + ", but associated: " + chunkRenderer.chunk().visible());
		}
		System.out.println("  is present ");
		System.out.println("  uploaded: " + chunkRenderer.terrainRenderer().uploaded());
		var graphicObject = chunkRenderer.terrainRenderer().graphicObject();
		if (graphicObject == null)
		{
			System.out.println("  graphic object is null");
			return;
		}
		System.out.println("  graphic object is not null");
		System.out.println("  attempt render in loop: " + chunkRenderer.attemptRenderInLoop());
		System.out.println("  attempt render in terrain: " + chunkRenderer.attemptRenderInTerrain());
		System.out.println("  attempt render: " + chunkRenderer.terrainRenderer().attemptRender());
		OpenGLValidator.validateGraphicObject("  ", this.shaderManager, graphicObject, false);
	}

	public void chunkUnloaded(IChunk chunkIn)
	{
		var key = chunkIn.key();
		synchronized (chunkStates)
		{
			chunkStates.putIfAbsent(key, new AtomicReference<>(ChunkState.UNLOADING));
		}
		synchronized (unloadQueue)
		{
			unloadQueue.add(chunkIn);
		}
	}

	public void render(Matrix4f projectionMatrixIn, Matrix4f viewMatrixIn)
	{
		shaderManager.attach();
		synchronized (chunkRenderers)
		{
			for (ChunkRenderer chunkRenderer : chunkRenderers.values())
			{
				chunkRenderer.attemptRenderInLoop(true);
				chunkRenderer.render();
			}
		}
		shaderManager.detach();
	}

	public void cleanup()
	{
		chunkRenderers.clear();
		shaderManager.cleanup();
	}

	@Getter
	public final static class ChunkRendererLoading
	{
		private final IChunk      chunk;
		private final Terrain     terrain;
		private final FloatBuffer vertices;
		private final IntBuffer   indices;

		public ChunkRendererLoading(IChunk chunkIn)
		{
			this.chunk   = chunkIn;
			this.terrain = chunkIn.terrain();
			var lodFactor   = this.terrain.lod();
			var numVertices = ((terrain.width() / lodFactor) + 1) * ((terrain.depth() / lodFactor) + 1);
			var numIndices  = ((terrain.width() / lodFactor)) * ((terrain.depth() / lodFactor)) * 6;
			this.vertices = BufferUtils.createFloatBuffer((int) (numVertices * 3));
			this.indices  = BufferUtils.createIntBuffer((int) numIndices);
		}

		public void prepare()
		{
			int   width = this.terrain.width();
			int   depth = this.terrain.depth();
			float lod   = this.terrain.lod();

			// Generate vertices
			for (float z = 0; z <= depth; z += lod)
			{
				for (float x = 0; x <= width; x += lod)
				{
					var point = this.terrain.point(x, z);
					if (point == null)
					{
						continue;
					}
					this.vertices.put(this.terrain.x() + x);
					this.vertices.put(this.terrain.y() + point.y);
					this.vertices.put(this.terrain.z() + z);
				}
			}
			this.vertices.flip();

			// Generate indices
			int vertexPerRow = (width / (int) lod) + 1;
			for (int z = 0; z < depth / lod; z++)
			{
				for (int x = 0; x < width / lod; x++)
				{
					int topLeft     = z * vertexPerRow + x;
					int topRight    = topLeft + 1;
					int bottomLeft  = topLeft + vertexPerRow;
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

	private enum ChunkState
	{
		LOADING, UNLOADING
	}
}
