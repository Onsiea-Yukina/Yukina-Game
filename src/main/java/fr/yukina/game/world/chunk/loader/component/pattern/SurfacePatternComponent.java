package fr.yukina.game.world.chunk.loader.component.pattern;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public class SurfacePatternComponent implements PatternComponent
{
	@Override
	public float validatePattern(ChunkLoaderContext context, int x, int z)
	{
		int centerX        = context.getCenterX();
		int centerZ        = context.getCenterZ();
		int renderDistance = context.getRenderDistance();
		var distX          = Math.abs(centerX - x);
		var distZ          = Math.abs(centerZ - z);

		return distX <= renderDistance && distZ <= renderDistance ? distX * distX + distZ * distZ : -1.0f;
	}
}