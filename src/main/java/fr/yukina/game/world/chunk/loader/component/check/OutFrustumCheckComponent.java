package fr.yukina.game.world.chunk.loader.component.check;

import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

public class OutFrustumCheckComponent implements CheckComponent
{
	@Override
	public boolean check(ChunkLoaderContext context, ChunkManager.ChunkLoading chunkLoading)
	{
		ChunkManager chunkManager = context.getChunkManager();
		return chunkManager.isOutFrustum(chunkLoading.x(), chunkLoading.z());
	}
}