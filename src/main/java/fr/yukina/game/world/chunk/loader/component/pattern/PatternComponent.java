package fr.yukina.game.world.chunk.loader.component.pattern;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public interface PatternComponent
{
	boolean validatePattern(ChunkLoaderContext context, int x, int z);
}