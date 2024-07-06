package fr.yukina.game.graphic.opengl.shader;

import lombok.Getter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ShaderManager
{
	private final static String                  SHADER_PATH          = "resources/shaders/";
	private static       BlockingQueue<Runnable> FILE_WATCHER_TASKS   = new LinkedBlockingQueue<>();
	private final static boolean                 FILE_WATCHER_VERBOSE = true;
	private static       ShaderFileWatcher       fileWatcher;

	private final @Getter Map<String, ShaderProgram> shaders;
	private @Getter       ShaderProgram              currentShader;

	public final static ShaderFileWatcher fileWatcher()
	{
		if (fileWatcher == null)
		{
			try
			{
				fileWatcher = new ShaderFileWatcher(SHADER_PATH, 1, TimeUnit.SECONDS, FILE_WATCHER_TASKS,
				                                    FILE_WATCHER_VERBOSE);
			}
			catch (IOException eIn)
			{
				throw new RuntimeException(eIn);
			}
		}

		return fileWatcher;
	}

	public final static void stopFileWatcher()
	{
		if (fileWatcher != null)
		{
			fileWatcher.stop();
			fileWatcher = null;
		}
	}

	public ShaderManager()
	{
		this.shaders            = new HashMap<>();
		this.FILE_WATCHER_TASKS = new LinkedBlockingQueue<>();
		this.currentShader      = null;
	}

	public void add(String name, ShaderProgram shader)
	{
		if (shaders.containsKey(name))
		{
			throw new RuntimeException("Shader already exists: " + name);
		}
		shaders.put(name, shader);
	}

	/* TODO public ShaderProgram create(String name, Map<Integer, String> shaderCodeMap, boolean verbose)
	{
		ShaderProgram shaderProgram = new ShaderProgram(shaderCodeMap, verbose);
		shaders.put(name, shaderProgram);
		return shaderProgram;
	}*/

	public ShaderProgram create(String name, Map<Integer, String> shaderFilePaths, boolean verboseIn)
	{
		if (shaders.containsKey(name))
		{
			throw new RuntimeException("Shader already exists: " + name);
		}

		ShaderProgram shaderProgram = new ShaderProgram(shaderFilePaths, verboseIn);
		shaders.put(name, shaderProgram);
		return shaderProgram;
	}

	public void attachShader(String name)
	{
		if (currentShader == null || !currentShader.equals(name))
		{
			if (currentShader != null)
			{
				shaders.get(currentShader).detach();
			}
			this.currentShader = shaders.get(name);
			if (currentShader == null)
			{
				throw new RuntimeException("Shader not found: " + name);
			}
			this.currentShader.attach();
		}
	}

	public void reloadShader(String name)
	{
		ShaderProgram shader = shaders.get(name);
		if (shader != null)
		{
			shader.reloadShaders();
		}
	}

	public void destroyShader(String name)
	{
		ShaderProgram shader = shaders.remove(name);
		if (shader != null)
		{
			shader.cleanup();
		}
	}

	public void enableFileWatcher(String shaderDirPath, long frequency, TimeUnit unit, boolean verbose)
	{
		try
		{
			fileWatcher = new ShaderFileWatcher(shaderDirPath, frequency, unit, FILE_WATCHER_TASKS, verbose);
			for (ShaderProgram shader : shaders.values())
			{
				fileWatcher.registerShaderProgram(shader);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void disableFileWatcher()
	{
		if (fileWatcher != null)
		{
			fileWatcher.stop();
			fileWatcher = null;
		}
	}

	public void update()
	{
		Runnable task;
		while ((task = FILE_WATCHER_TASKS.poll()) != null)
		{
			task.run();
		}
	}

	public boolean isLoaded(String name)
	{
		return shaders.containsKey(name);
	}

	public ShaderProgram get(String name)
	{
		return shaders.get(name);
	}

	// New methods for managing shader uniforms and attributes
	public int uniformLocation(String shaderName, String uniformName)
	{
		ShaderProgram shader = shaders.get(shaderName);
		if (shader != null)
		{
			return shader.uniform(uniformName);
		}
		throw new RuntimeException("Shader not found: " + shaderName);
	}

	public void log(String shaderName)
	{
		ShaderProgram shader = shaders.get(shaderName);
		if (shader != null)
		{
			shader.logStatus();
		}
		else
		{
			System.out.println("Shader not found: " + shaderName);
		}
	}

	public void cleanup()
	{
		disableFileWatcher();
		for (ShaderProgram shader : shaders.values())
		{
			shader.cleanup();
		}
		shaders.clear();
		currentShader = null;
	}
}