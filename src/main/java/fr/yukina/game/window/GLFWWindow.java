package fr.yukina.game.window;

import lombok.Getter;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.IntBuffer;

@Getter
public class GLFWWindow implements IWindow
{
	private final int    width;
	private final int    height;
	private final String title;
	private final int    framerate;
	private final int    synchronization;

	private long handle;

	public GLFWWindow(int widthIn, int heightIn, String titleIn, int framerateIn, int synchronizationIn)
	{
		this.width           = widthIn;
		this.height          = heightIn;
		this.title           = titleIn;
		this.framerate       = framerateIn;
		this.synchronization = synchronizationIn;
	}

	@Override
	public void initialize()
	{
		// Initialize GLFW
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit())
		{
			throw new RuntimeException("Failed to initialize GLFW");
		}

		// Configure GLFW
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_REFRESH_RATE, this.framerate);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_DEBUG_CONTEXT, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_DEBUG, GLFW.GLFW_TRUE);

		// Create the window
		this.handle = GLFW.glfwCreateWindow(this.width, this.height, this.title, 0, 0);
		if (this.handle == MemoryUtil.NULL)
		{
			throw new RuntimeException("Failed to create GLFW window");
		}

		GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) ->
		{
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
			{
				GLFW.glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
			}
		});

		// Set the window position to the center of the monitor
		try (var stack = MemoryStack.stackPush())
		{
			IntBuffer pWidth  = stack.mallocInt(1); // int*
			IntBuffer pHeight = stack.mallocInt(1); // int*

			GLFW.glfwGetWindowSize(handle, pWidth, pHeight);
			GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

			// Center the window
			GLFW.glfwSetWindowPos(handle, (vidmode.width() - pWidth.get(0)) / 2,
			                      (vidmode.height() - pHeight.get(0)) / 2);
		}

		GLFW.glfwMakeContextCurrent(this.handle);
		GLFW.glfwSwapInterval(this.synchronization);
		GLFW.glfwShowWindow(this.handle);
	}

	@Override
	public void pollEvents()
	{
		GLFW.glfwPollEvents();
	}

	@Override
	public void swapBuffers()
	{
		GLFW.glfwSwapBuffers(this.handle);
	}

	@Override
	public boolean shouldClose()
	{
		return GLFW.glfwWindowShouldClose(this.handle);
	}

	@Override
	public void cleanup()
	{
		Callbacks.glfwFreeCallbacks(this.handle);
		GLFW.glfwDestroyWindow(this.handle);

		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}
}
