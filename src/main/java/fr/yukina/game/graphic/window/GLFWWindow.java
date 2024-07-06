package fr.yukina.game.graphic.window;

import lombok.Getter;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

@Getter
public class GLFWWindow
{
	private long       handle;
	private int        width;
	private int        height;
	private String     title;
	private int        framerate;
	private int        synchronisation;
	private boolean    antiAliasing;
	private long       monitor;
	private GLFWWindow sharedWindow;

	GLFWWindow(int widthIn, int heightIn, String titleIn, int framerateIn, int synchronisationIn,
	           boolean antiAliasingIn, long monitorIn)
	{
		this(widthIn, heightIn, titleIn, framerateIn, synchronisationIn, antiAliasingIn, monitorIn, null);
	}

	GLFWWindow(int widthIn, int heightIn, String titleIn, int framerateIn, int synchronisationIn,
	           boolean antiAliasingIn, long monitorIn, GLFWWindow sharedWindowIn)
	{
		this.width           = widthIn;
		this.height          = heightIn;
		this.title           = titleIn;
		this.framerate       = framerateIn;
		this.synchronisation = synchronisationIn;
		this.antiAliasing    = antiAliasingIn;
		this.monitor         = monitorIn;
		this.sharedWindow    = sharedWindowIn;
	}

	public final void initialize()
	{
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		glfwWindowHint(GLFW_REFRESH_RATE, this.framerate);
		if (this.antiAliasing)
		{
			glfwWindowHint(GLFW_SAMPLES, 4);
		}

		this.handle = glfwCreateWindow(this.width, this.height, this.title, NULL,
		                               this.sharedWindow != null ? this.sharedWindow.handle : NULL);
		if (this.handle == NULL)
		{
			throw new RuntimeException("Failed to create the GLFW window");
		}

		glfwSetKeyCallback(this.handle, (window, key, scancode, action, mods) ->
		{
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
			{
				glfwSetWindowShouldClose(window, true);
			}
		});

		glfwMakeContextCurrent(this.handle);
		glfwSwapInterval(this.synchronisation);
		if (this.monitor == NULL)
		{
			this.monitor = glfwGetPrimaryMonitor();
		}

		placeWindow(this.monitor);
	}

	public final void placeWindow(long monitorIn)
	{
		try (MemoryStack stack = stackPush())
		{
			IntBuffer pWidth  = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			glfwGetWindowSize(this.handle, pWidth, pHeight);

			var x = new int[1];
			var y = new int[1];
			glfwGetMonitorPos(monitorIn, x, y);

			GLFWVidMode vidmode = glfwGetVideoMode(monitorIn);

			glfwSetWindowPos(this.handle, x[0] + (vidmode.width() - pWidth.get(0)) / 2,
			                 y[0] + (vidmode.height() - pHeight.get(0)) / 2);
		}
	}

	public final void show()
	{
		glfwShowWindow(this.handle);
	}

	public final boolean shouldClose()
	{
		return glfwWindowShouldClose(this.handle);
	}

	public final void makeCurrent()
	{
		glfwMakeContextCurrent(this.handle);
	}

	public final void leaveCurrent()
	{
		glfwMakeContextCurrent(NULL);
	}

	public final void hide()
	{
		glfwHideWindow(this.handle);
	}

	public final double[] mousePosition()
	{
		double[] x = new double[1];
		double[] y = new double[1];

		glfwGetCursorPos(this.handle, x, y);

		double[] position = new double[2];
		position[0] = x[0];
		position[1] = y[0];

		return position;
	}

	public final boolean pressed(int keyIn)
	{
		return glfwGetKey(this.handle, keyIn) == GLFW_PRESS;
	}

	public final void swapBuffers()
	{
		glfwSwapBuffers(this.handle);
	}

	public final void cleanup()
	{
		if (this.handle != MemoryUtil.NULL)
		{
			glfwFreeCallbacks(this.handle);
			glfwDestroyWindow(this.handle);
		}
	}
}