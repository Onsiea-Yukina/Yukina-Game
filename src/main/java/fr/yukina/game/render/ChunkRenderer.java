package fr.yukina.game.render;

import fr.yukina.game.world.chunk.IChunk;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ChunkRenderer
{
	private final           IChunk          chunk;
	private final @Getter   TerrainRenderer terrainRenderer;
	private @Getter @Setter boolean         attemptRenderInLoop;
	private @Getter @Setter boolean         attemptRenderInTerrain;

	public ChunkRenderer(IChunk chunkIn)
	{
		this.chunk           = chunkIn;
		this.terrainRenderer = new TerrainRenderer(chunkIn.terrain());
	}

	public void render()
	{
		this.attemptRenderInTerrain(false);
		if (this.chunk.visible())
		{
			this.attemptRenderInTerrain(true);
			this.terrainRenderer.render();
		}
	}

	public void cleanup()
	{
		this.terrainRenderer.cleanup();
	}
}