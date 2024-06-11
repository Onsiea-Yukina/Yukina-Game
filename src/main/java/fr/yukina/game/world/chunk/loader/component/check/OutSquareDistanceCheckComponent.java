package fr.yukina.game.world.chunk.loader.component.check;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;
import fr.yukina.game.world.terrain.Terrain;

public class OutSquareDistanceCheckComponent implements CheckComponent
{
	@Override
	public boolean check(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading)
	{
		int centerX        = context.getCenterX() * Terrain.WIDTH;
		int centerZ        = context.getCenterZ() * Terrain.DEPTH;
		int renderDistance = context.getRenderDistance();

		int chunkX = chunkLoading.x() * Terrain.WIDTH;
		int chunkZ = chunkLoading.z() * Terrain.DEPTH;

		return chunkX < centerX - renderDistance * Terrain.WIDTH || chunkX > centerX + renderDistance * Terrain.WIDTH
		       || chunkZ < centerZ - renderDistance * Terrain.DEPTH
		       || chunkZ > centerZ + renderDistance * Terrain.DEPTH;
	}
}