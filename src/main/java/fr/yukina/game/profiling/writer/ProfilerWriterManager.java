package fr.yukina.game.profiling.writer;

import java.util.HashMap;
import java.util.Map;

public class ProfilerWriterManager
{
	private final Map<String, ProfilerWriter> writers;

	public ProfilerWriterManager()
	{
		this.writers = new HashMap<>();
	}

	public void add(String nameIn, ProfilerWriter writerIn)
	{
		this.writers.put(nameIn, writerIn);
	}

	public final void write()
	{
		long start = System.nanoTime();
		for (ProfilerWriter writer : this.writers.values())
		{
			writer.write(start);
		}
	}

	public final void remove(String nameIn)
	{
		this.writers.remove(nameIn);
	}

	public final void clear()
	{
		this.writers.clear();
	}
}
