package fr.yukina.game.logic.terrain.calculation;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.action.Action;
import fr.yukina.game.logic.terrain.brush.Brush;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class Calculation implements Runnable
{
	private final Terrain terrain;
	private final Brush   brush;
	private final Action  action;

	private final float x;
	private final float y;
	private final float z;

	private final Map<Float, Map<Float, Float>> heights;

	private @Setter boolean needApply;

	private @Setter  long    timeLimit;
	private          long    last;
	private @Setter  boolean prioritized;
	private volatile boolean running;
	private          boolean finished;
	private volatile boolean stopped;
	private          Thread  thread;

	public Calculation(Terrain terrainIn, Brush brushIn, Action actionIn)
	{
		this.terrain   = terrainIn;
		this.brush     = brushIn;
		this.action    = actionIn;
		this.x         = brushIn.x();
		this.y         = brushIn.y();
		this.z         = brushIn.z();
		this.heights   = new ConcurrentHashMap<>();
		this.needApply = false;

		this.timeLimit   = 5_000_000L;
		this.prioritized = false;

		this.reset();
		this.start();
	}

	public final void priorize()
	{
		this.prioritized = true;
	}

	public final void add(float xIn, float zIn, float heightIn)
	{
		var xMap = this.heights.computeIfAbsent(xIn, k -> new ConcurrentHashMap<>());
		xMap.put(zIn, heightIn);
	}

	public final void start()
	{
		this.running  = true;
		this.finished = false;
		this.stopped  = false;
		this.thread   = new Thread(this);
		this.last     = System.nanoTime();
		this.thread.start();
	}

	@Override
	public void run()
	{
		var success = new AtomicBoolean(true);
		System.out.println("CALCULATION APPLY ! " + this.brush.name());
		this.brush.apply(this.terrain, () -> this.running, this.action, (xIn, zIn, yIn) ->
		{
			System.out.println("BRUSH APPLY ! " + this.brush.name());
			synchronized (this)
			{
				if (!this.running)
				{
					success.set(false);
					return;
				}
			}

			while (!this.prioritized && System.nanoTime() - this.last > this.timeLimit)
			{
				synchronized (this)
				{
					if (!this.running)
					{
						success.set(false);
						return;
					}
				}
			}
			this.last = System.nanoTime();

			this.add(xIn, zIn, yIn);
		});
		this.reset();
		synchronized (this)
		{
			this.finished = success.get();
		}
	}

	private synchronized void reset()
	{
		this.running  = false;
		this.finished = false;
		this.stopped  = true;
	}

	public final void clear()
	{
		this.heights.clear();
	}

	public final void stop()
	{
		synchronized (this)
		{
			this.running = false;
		}
		long last = System.nanoTime();
		while (!this.stopped)
		{
			if (System.nanoTime() - last > 1_000_000_000L)
			{
				if (this.thread != null)
				{
					this.thread.interrupt();
				}
				System.err.println(
						"[ERROR] Calculation : Failed to stop thread calculation. " + ((System.nanoTime() - last)
						                                                               / 1_000_000_000L) + "s "
						+ "timeout");
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
}