package fr.yukina.game.world.chunk.loader.component.execution;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public interface ExecutionStrategyComponent
{
	void executeLoading(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading);

	void executeUnloading(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading);
}