package fr.yukina.game.world.chunk;

import fr.yukina.game.ILink;
import fr.yukina.game.world.terrain.ITerrainGenerator;
import lombok.experimental.Delegate;

public class ChunkLink implements ILink<IChunk>, IChunk
{
	private @Delegate IChunk chunk;

	public ChunkLink(String keyIn, int xIn, int yIn, int zIn, int widthIn, int depthIn, ITerrainGenerator generatorIn)
	{
		this.chunk = Chunk.get(keyIn, xIn, yIn, zIn, widthIn, depthIn, generatorIn);
	}

	public ChunkLink(Chunk chunkIn)
	{
		this.chunk = chunkIn;
	}

	@Override
	public IChunk link()
	{
		return this.chunk;
	}

	@Override
	public ILink<IChunk> link(final IChunk chunkIn)
	{
		this.chunk = chunkIn;
		return this;
	}
}