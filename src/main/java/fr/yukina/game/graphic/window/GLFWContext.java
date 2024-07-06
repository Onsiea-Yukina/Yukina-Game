package fr.yukina.game.graphic.window;

import lombok.experimental.Delegate;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.glfw.GLFW.*;

public class GLFWContext
{
	private final           Map<String, GLFWWindow> windows;
	private final @Delegate GLFWMonitors            monitors;

	public GLFWContext()
	{
		GLFWErrorCallback.createPrint(System.err).set();
		if (!glfwInit())
		{
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		this.windows  = new ConcurrentHashMap<>();
		this.monitors = new GLFWMonitors();
	}

	public final GLFWWindow create(int widthIn, int heightIn, String titleIn, int framerateIn, int synchronisationIn,
	                               boolean antiAliasingIn, long monitorIn)
	{
		return this.create(titleIn, widthIn, heightIn, titleIn, framerateIn, synchronisationIn, antiAliasingIn,
		                   monitorIn);
	}

	public final GLFWWindow create(int widthIn, int heightIn, String titleIn, int framerateIn, int synchronisationIn,
	                               boolean antiAliasingIn, long monitorIn, GLFWWindow sharedWindowIn)
	{
		return this.create(titleIn, widthIn, heightIn, titleIn, framerateIn, synchronisationIn, antiAliasingIn,
		                   monitorIn, sharedWindowIn);
	}

	public final GLFWWindow create(String nameIn, int widthIn, int heightIn, String titleIn, int framerateIn,
	                               int synchronisationIn, boolean antiAliasingIn, long monitorIn)
	{
		return this.create(nameIn, widthIn, heightIn, titleIn, framerateIn, synchronisationIn, antiAliasingIn,
		                   monitorIn, null);
	}

	public final GLFWWindow create(String nameIn, int widthIn, int heightIn, String titleIn, int framerateIn,
	                               int synchronisationIn, boolean antiAliasingIn, long monitorIn,
	                               GLFWWindow sharedWindowIn)
	{
		var window = new GLFWWindow(widthIn, heightIn, titleIn, framerateIn, synchronisationIn, antiAliasingIn,
		                            monitorIn, sharedWindowIn);
		var name = nameIn;
		var i    = 0;
		while (this.windows.containsKey(name))
		{
			name = name + "_" + i;
			i++;
		}
		windows.put(name, window);
		return window;
	}

	public final GLFWWindow get(String nameIn)
	{
		return this.windows.get(nameIn);
	}

	public final void pollEvents()
	{
		glfwPollEvents();
	}

	public void destroy()
	{
		for (var window : this.windows.values())
		{
			window.cleanup();
		}

		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}
}
