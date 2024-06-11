package fr.yukina.game.world.chunk.loader.component.log;

import fr.yukina.game.world.chunk.loader.component.ChunkLoaderComponent;
import fr.yukina.game.world.chunk.loader.component.ChunkLoaderContext;

import java.util.logging.Logger;

public class LogComponent implements ChunkLoaderComponent
{
	private static final Logger LOGGER = Logger.getLogger(LogComponent.class.getName());

	@Override
	public void execute(ChunkLoaderContext context)
	{
		for (var chunk : context.getChunkManager().chunks().values())
		{
			LOGGER.info(String.format("Chunk at (%d, %d) is %s", chunk.terrain().x(), chunk.terrain().z(),
			                          chunk.visible() ? "visible" : "not visible"));
		}
	}
}