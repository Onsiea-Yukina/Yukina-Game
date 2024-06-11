package fr.yukina.game.world.chunk.loader.component.states;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public class PlayerInNewChunkStateComponent implements StateComponent
{
	@Override
	public boolean shouldExecute(ChunkLoaderContext context)
	{
		return context.getChunkManager().player().updateState().hasMoveOneChunk();
	}
}