package fr.yukina.game.graphic.opengl.shader;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ShaderFileWatcher
{
	private final Path                     shaderDir;
	private final ScheduledExecutorService executorService;
	private final Set<ShaderProgram>       shaderPrograms;
	private final BlockingQueue<Runnable>  mainThreadTasks;
	private       WatchService             watchService;
	private       boolean                  verbose;
	private       boolean                  running;

	public ShaderFileWatcher(String shaderDirPathIn, long frequencyIn, TimeUnit unitIn,
	                         BlockingQueue<Runnable> mainThreadTasksIn, boolean verboseIn) throws IOException
	{
		this.shaderDir       = Paths.get(shaderDirPathIn);
		this.shaderPrograms  = new HashSet<>();
		this.mainThreadTasks = mainThreadTasksIn;
		this.executorService = Executors.newScheduledThreadPool(1);
		this.watchService    = FileSystems.getDefault().newWatchService();
		this.shaderDir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY,
		                        StandardWatchEventKinds.ENTRY_DELETE);
		this.verbose = verboseIn;
		this.running = true;

		this.executorService.scheduleAtFixedRate(this::processEvents, 0, frequencyIn, unitIn);
	}

	public void registerShaderProgram(ShaderProgram shaderProgram)
	{
		shaderPrograms.add(shaderProgram);
	}

	private void processEvents()
	{
		if (verbose)
		{
			System.out.println("Checking for changes in " + shaderDir + " ...");
		}

		try
		{
			WatchKey key;
			while (running && (key = watchService.poll()) != null)
			{
				for (WatchEvent<?> event : key.pollEvents())
				{
					WatchEvent.Kind<?> kind = event.kind();
					if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_DELETE)
					{
						Path changedFilePath = shaderDir.resolve((Path) event.context());
						if (verbose)
						{
							System.out.println("Detected change: " + event.kind() + " in " + changedFilePath);
						}
						for (ShaderProgram shaderProgram : shaderPrograms)
						{
							if (shaderProgram.isShaderFile(changedFilePath))
							{
								mainThreadTasks.offer(shaderProgram::reloadShaders);
							}
						}
					}
				}
				key.reset();
			}
		}
		catch (ClosedWatchServiceException e)
		{
			System.out.println("WatchService closed.");
		}
	}

	public void stop()
	{
		this.running = false;
		executorService.shutdown();
		try
		{
			watchService.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}