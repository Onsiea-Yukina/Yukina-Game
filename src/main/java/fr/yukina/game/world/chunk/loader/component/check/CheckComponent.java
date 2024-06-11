package fr.yukina.game.world.chunk.loader.component.check;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public interface CheckComponent
{
	boolean check(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading);
}