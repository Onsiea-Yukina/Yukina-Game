package fr.yukina.game.world.chunk.loader.component.validation;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderComponent;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

import java.util.logging.Logger;

public class ValidationComponent implements ChunkLoaderComponent
{
	private static final Logger LOGGER = Logger.getLogger(ValidationComponent.class.getName());

	@Override
	public void execute(ChunkLoaderContext context)
	{
		for (var chunk : context.getChunkManager().chunks().values())
		{
			if (context.getChunkManager().renderDistance().isOut(chunk))
			{
				LOGGER.warning(String.format("Chunk at (%d, %d) is out of render distance", chunk.terrain().x(),
				                             chunk.terrain().z()));
			}
			if (context.getChunkManager().isOutFrustum(chunk))
			{
				LOGGER.warning(
						String.format("Chunk at (%d, %d) is out of frustum", chunk.terrain().x(),
						              chunk.terrain().z()));
			}
		}
	}
}