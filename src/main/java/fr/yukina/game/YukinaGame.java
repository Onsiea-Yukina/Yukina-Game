package fr.yukina.game;

import fr.yukina.game.profiling.Profiler;
import fr.yukina.game.profiling.configuration.Configuration;
import fr.yukina.game.profiling.metric.data.DurationMetricDataGenerator;
import fr.yukina.game.profiling.writer.ProfilerCSVWriter;
import fr.yukina.game.profiling.writer.ProfilerWriter;
import fr.yukina.game.window.IWindow;
import fr.yukina.game.world.World;
import lombok.Getter;

@Getter
public class YukinaGame implements IGame
{
	private final Profiler   profiler;
	private final IGameLogic world;
	private final Player     player;

	public YukinaGame()
	{
		var camera = new FPSCamera((float) Math.toRadians(90.0f), 1920.0f / 1080.0f, 0.1f, 10000.0f);
		this.player   = new Player(camera);
		this.world    = new World(this.player);
		this.profiler = new Profiler();
		this.profiler.addMetricFunction("duration", new DurationMetricDataGenerator());
		this.profiler.addMetricFunction("FPS", Integer.class);
		this.profiler.addMetricInfo("FPS", "Frames per second", "frame/s");

		this.profiler.addMetricFunction("memory-usage", () ->
		{
			return (int) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024;
		});
		this.profiler.addMetricInfo("memory-usage", "Memory usage", "MB");

		this.profiler.addConfiguration("detailed", "FPS", "memory-usage");
		this.profiler.profileManager().basicConfiguration(new Configuration("duration"));

		this.profiler.addWriter("csv-writer",
		                        new ProfilerWriter(new ProfilerCSVWriter(this.profiler, "profiling.csv")));
	}

	@Override
	public void initialize()
	{
		for (var i = 0; i < 25; i++)
		{
			this.profiler.start("main-initialize");
			try
			{
				Thread.sleep(100);
			}
			catch (InterruptedException eIn)
			{
				throw new RuntimeException(eIn);
			}
			this.profiler.end("main-initialize");
		}
		System.exit(-1);
		this.world.initialize();
		this.player.initialize();
	}

	@Override
	public void input(final IWindow windowIn)
	{
		this.world.input(windowIn);
		this.player.input(windowIn);
	}

	@Override
	public void update()
	{

		this.world.update();
		this.player.update();
	}

	@Override
	public void cleanup()
	{
		this.world.cleanup();
		this.player.cleanup();
	}
}