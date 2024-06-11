package fr.yukina.game.world.chunk.loader.component;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.check.CheckComponent;
import fr.yukina.game.world.chunk.loader.component.execution.ExecutionStrategyComponent;
import fr.yukina.game.world.chunk.loader.component.pattern.PatternComponent;
import fr.yukina.game.world.terrain.Terrain;

import java.util.List;
import java.util.logging.Logger;

public class UnloadingComponent implements ChunkLoaderComponent
{
	private static final Logger LOGGER = Logger.getLogger(UnloadingComponent.class.getName());

	private final PatternComponent           patternComponent;
	private final List<CheckComponent>       checkComponents;
	private final ExecutionStrategyComponent executionStrategy;

	public UnloadingComponent(PatternComponent patternComponent, List<CheckComponent> checkComponents,
	                          ExecutionStrategyComponent executionStrategy)
	{
		this.patternComponent  = patternComponent;
		this.checkComponents   = checkComponents;
		this.executionStrategy = executionStrategy;
	}

	@Override
	public void execute(ChunkLoaderContext context)
	{
		ChunkManager chunkManager = context.getChunkManager();

		for (var chunk : chunkManager.chunks().values())
		{
			int chunkX = chunk.terrain().x() / Terrain.WIDTH;
			int chunkZ = chunk.terrain().z() / Terrain.DEPTH;

			if (!patternComponent.validatePattern(context, chunkX, chunkZ))
			{
				ChunkManager.ChunkLoading chunkLoading = new ChunkManager.ChunkLoading(chunkX, chunkZ, () ->
				{
					chunk.visible(false);
					chunk.needUnload(true);
					chunk.cleanup();
					chunkManager.chunks().remove(chunk.key());
					for (var listener : chunkManager.unloadedListeners())
					{
						listener.onChunk(chunk);
					}
					LOGGER.info(String.format("Unloaded chunk at (%d, %d) with key %s", chunkX, chunkZ, chunk.key()));
					return null;
				}, chunk.key());

				boolean canUnload = true;
				for (CheckComponent checkComponent : checkComponents)
				{
					if (!checkComponent.check(context, chunkLoading))
					{
						canUnload = false;
						break;
					}
				}

				if (canUnload)
				{
					executionStrategy.executeUnloading(context, chunkLoading);
				}
			}
		}
	}
}