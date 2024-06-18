package fr.yukina.game.world.chunk.loader.component.pattern;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public interface PatternComponent
{
	/**
	 * @param context
	 * @param x
	 * @param z
	 * @return distance if is valid, -1 otherwise
	 */
	float validatePattern(ChunkLoaderContext context, int x, int z);
}