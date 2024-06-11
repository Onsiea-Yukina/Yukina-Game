package fr.yukina.game.world.chunk.loader.component.states;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public interface StateComponent
{
	boolean shouldExecute(ChunkLoaderContext context);
}