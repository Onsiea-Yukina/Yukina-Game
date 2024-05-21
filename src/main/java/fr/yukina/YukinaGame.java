package fr.yukina;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLDebugMessageCallback;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL46.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class YukinaGame
{
	private long                   window;
	private World                  world;
	private int                    width  = 1920;
	private int                    height = 1080;
	private GLDebugMessageCallback debugCallback;

	public void run()
	{
		init();
		loop();

		glfwDestroyWindow(window);
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init()
	{
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit())
		{
			throw new IllegalStateException("Unable to initialize GLFW");
		}

		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

		window = glfwCreateWindow(width, height, "Yukina Game", NULL, NULL);
		if (window == NULL)
		{
			throw new RuntimeException("Failed to create the GLFW window");
		}

		glfwSetKeyCallback(window, (window, key, scancode, action, mods) ->
		{
			if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
			{
				glfwSetWindowShouldClose(window, true);
			}
		});

		glfwSetCursorPosCallback(window, (window, xpos, ypos) ->
		{
			// Handle mouse movement
		});

		glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

		glfwMakeContextCurrent(window);
		glfwSwapInterval(1);
		glfwShowWindow(window);

		GL.createCapabilities();

		// Enable OpenGL debug context
		glEnable(GL_DEBUG_OUTPUT);
		glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
		debugCallback = GLDebugMessageCallback.create(new DebugCallback());
		glDebugMessageCallback(debugCallback, 0);
		glDebugMessageControl(GL_DONT_CARE, GL_DONT_CARE, GL_DONT_CARE, (IntBuffer) null, true);

		// Set viewport and clear color
		glViewport(0, 0, width, height);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

		world = new World(window, width, height);
	}

	private void loop()
	{
		while (!glfwWindowShouldClose(window))
		{
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			world.update(window);
			world.render();

			glfwSwapBuffers(window);
			glfwPollEvents();
		}
	}
}