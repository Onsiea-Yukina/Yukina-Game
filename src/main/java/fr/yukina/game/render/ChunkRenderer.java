package fr.yukina.game.render;

import fr.yukina.game.world.chunk.Chunk;
import lombok.Getter;

public class ChunkRenderer
{
	private final         Chunk           chunk;
	private final @Getter TerrainRenderer terrainRenderer;

	public ChunkRenderer(Chunk chunkIn)
	{
		this.chunk           = chunkIn;
		this.terrainRenderer = new TerrainRenderer(chunkIn.terrain());
	}

	public void render()
	{
		if (this.chunk.visible())
		{
			this.terrainRenderer.render();
		}
	}

	public void cleanup()
	{
		this.terrainRenderer.cleanup();
	}
}