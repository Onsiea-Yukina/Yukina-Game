package fr.yukina.game.world.chunk;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

public class ChunkLoaderTask implements Runnable
{
	private final Chunk                             chunk;
	private final ConcurrentMap<String, Chunk>      chunks;
	private final List<ChunkManager.IChunkListener> loadedListeners;

	public ChunkLoaderTask(Chunk chunk, ConcurrentMap<String, Chunk> chunks,
	                       List<ChunkManager.IChunkListener> loadedListeners)
	{
		this.chunk           = chunk;
		this.chunks          = chunks;
		this.loadedListeners = loadedListeners;
	}

	@Override
	public void run()
	{
		this.chunk.generate();
		this.chunks.put(this.chunk.key(), this.chunk);
		for (var listener : this.loadedListeners)
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