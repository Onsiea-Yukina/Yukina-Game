package fr.yukina.game.world.chunk.loader.component.execution;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public class QueuedExecutionStrategyComponent implements ExecutionStrategyComponent
{
	@Override
	public void executeLoading(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading)
	{
		context.getChunkLoadingQueue().add(chunkLoading);
	}

	@Override
	public void executeUnloading(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading)
	{
		context.getChunkLoadingQueue().add(chunkLoading);
	}
}