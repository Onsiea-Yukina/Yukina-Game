package fr.yukina.game.world.chunk.loader;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderComponent;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;
import fr.yukina.game.world.chunk.loader.component.states.StateComponent;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

public class ModularChunkLoader implements IChunkLoader
{
	private static final Logger LOGGER = Logger.getLogger(ModularChunkLoader.class.getName());

	private final ChunkManager                     chunkManager;
	private final Queue<ChunkManager.ChunkLoading> chunkLoadingQueue;
	private       List<ChunkLoaderComponent>       components;
	private       List<StateComponent>             stateComponents;

	public ModularChunkLoader(ChunkManager chunkManagerIn)
	{
		this.chunkManager      = chunkManagerIn;
		this.chunkLoadingQueue = new LinkedList<>();
		this.components        = new ArrayList<>();
		this.stateComponents   = new ArrayList<>();
	}

	@Override
	public void update()
	{
		ChunkLoaderContext context = new ChunkLoaderContext(chunkManager, chunkLoadingQueue);

		boolean shouldExecute = true;
		for (StateComponent stateComponent : stateComponents)
		{
			if (!stateComponent.shouldExecute(context))
			{
				shouldExecute = false;
				break;
			}
		}

		if (shouldExecute)
		{
			for (ChunkLoaderComponent component : components)
			{
				component.execute(context);
			}
		}

		while (!chunkLoadingQueue.isEmpty())
		{
			ChunkManager.ChunkLoading chunkLoading = chunkLoadingQueue.poll();
			if (chunkLoading != null)
			{
				chunkLoading.loadFunction().get();
			}
		}
	}

	@Override
	public void cleanup()
	{
		// Implement any necessary cleanup logic here
	}

	public void setComponents(List<ChunkLoaderComponent> components)
	{
		this.components = components;
	}

	public void setStateComponents(List<StateComponent> stateComponents)
	{
		this.stateComponents = stateComponents;
	}
}