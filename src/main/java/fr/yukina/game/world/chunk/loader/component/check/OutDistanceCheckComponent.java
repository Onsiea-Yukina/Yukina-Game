package fr.yukina.game.world.chunk.loader.component.check;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;
import fr.yukina.game.world.terrain.Terrain;

public class OutDistanceCheckComponent implements CheckComponent
{
	@Override
	public boolean check(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading)
	{
		ChunkManager chunkManager = context.getChunkManager();
		return chunkManager.renderDistance().isOut(chunkLoading.x() * Terrain.WIDTH, chunkLoading.z() * Terrain.DEPTH);
	}
}