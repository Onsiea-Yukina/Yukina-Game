package fr.yukina.game.world.chunk.loader.component.pattern;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;
import fr.yukina.game.world.terrain.Terrain;

public class CircularPatternComponent implements PatternComponent
{
	@Override
	public float validatePattern(ChunkLoaderContext context, int x, int z)
	{
		int   centerX            = context.getCenterX();
		int   centerZ            = context.getCenterZ();
		float maxDistanceSquared = context.getMaxDistanceSquared();

		float dx              = centerX - x;
		float dz              = centerZ - z;
		float distanceSquared = dx * dx * Terrain.WIDTH * Terrain.WIDTH + dz * dz * Terrain.DEPTH * Terrain.DEPTH;

		return distanceSquared <= maxDistanceSquared ? distanceSquared : -1.0f;
	}
}