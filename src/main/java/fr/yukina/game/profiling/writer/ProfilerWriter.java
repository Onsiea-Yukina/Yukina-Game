package fr.yukina.game.profiling.writer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ProfilerWriter
{
	private final IProfilerWriter writer;
	private       long            interval;
	private       long            lastTime;

	public ProfilerWriter(IProfilerWriter writerIn)
	{
		this.writer   = writerIn;
		this.interval = -1;
		this.lastTime = 0;
	}

	public final void write(long startIn)
	{
		if (this.lastTime - startIn < this.interval && this.interval > -1)
		{
			return;
		}

		this.writer.write();
		this.lastTime = startIn;
	}
}