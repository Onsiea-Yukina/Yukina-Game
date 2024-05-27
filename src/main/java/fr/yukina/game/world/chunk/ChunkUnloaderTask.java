package fr.yukina.game.world.chunk;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ChunkUnloaderTask implements Runnable
{
	private final Chunk                             chunk;
	private final ConcurrentMap<String, Chunk>      chunks;
	private final List<ChunkManager.IChunkListener> unloadedListeners;

	public ChunkUnloaderTask(Chunk chunk, ConcurrentMap<String, Chunk> chunks,
	                         List<ChunkManager.IChunkListener> unloadedListeners)
	{
		this.chunk             = chunk;
		this.chunks            = chunks;
		this.unloadedListeners = unloadedListeners;
	}

	@Override
	public void run()
	{
		this.chunk.cleanup();
		this.chunks.remove(this.chunk.key());
		for (var listener : this.unloadedListeners)
		{
			listener.onChunk(this.chunk);
		}
		try
		{
			Thread.sleep(1);
		}
		catch (InterruptedException eIn)
		{
			throw new RuntimeException(eIn);
		}
	}
}