package fr.yukina.game.world.chunk.loader.component;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.check.CheckComponent;
import fr.yukina.game.world.chunk.loader.component.execution.ExecutionStrategyComponent;
import fr.yukina.game.world.chunk.loader.component.pattern.PatternComponent;

import java.util.List;
import java.util.logging.Logger;

public class LoadingComponent implements ChunkLoaderComponent
{
	private static final Logger LOGGER = Logger.getLogger(LoadingComponent.class.getName());

	private final PatternComponent           patternComponent;
	private final List<CheckComponent>       checkComponents;
	private final ExecutionStrategyComponent executionStrategy;

	public LoadingComponent(PatternComponent patternComponent, List<CheckComponent> checkComponents,
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

		int centerX        = context.getCenterX();
		int centerZ        = context.getCenterZ();
		int renderDistance = context.getRenderDistance();

		for (int x = centerX - renderDistance; x <= centerX + renderDistance; x++)
		{
			for (int z = centerZ - renderDistance; z <= centerZ + renderDistance; z++)
			{
				var distance = patternComponent.validatePattern(context, x, z);
				if (distance < 0)
				{
					continue;
				}

				String key = chunkManager.key(x, z);

				if (!chunkManager.chunks().containsKey(key))
				{
					final int finalX = x;
					final int finalZ = z;

					ChunkManager.ChunkLoading chunkLoading = new ChunkManager.ChunkLoading(x, z, () ->
					{
						LOGGER.info(String.format("Loading chunk at %s", key));
						return chunkManager.loadChunk(key, finalX, finalZ, distance);
					}, key);

					boolean canLoad = true;
					for (CheckComponent checkComponent : checkComponents)
					{
						if (!checkComponent.check(context, chunkLoading))
						{
							canLoad = false;
							break;
						}
					}

					if (canLoad)
					{
						executionStrategy.executeLoading(context, chunkLoading);
					}
				}
			}
		}
	}
}