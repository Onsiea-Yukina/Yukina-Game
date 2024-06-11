package fr.yukina.game.world.chunk.loader.component.pattern;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public class SurfacePatternComponent implements PatternComponent
{
	@Override
	public boolean validatePattern(ChunkLoaderContext context, int x, int z)
	{
		int centerX        = context.getCenterX();
		int centerZ        = context.getCenterZ();
		int renderDistance = context.getRenderDistance();

		return Math.abs(centerX - x) <= renderDistance && Math.abs(centerZ - z) <= renderDistance;
	}
}