package fr.yukina.game.graphic.window.handler;

import lombok.Getter;
import org.lwjgl.glfw.GLFW;

public class InputHandler
{
	private @Getter double mouseX;
	private @Getter double mouseY;

	private final Subscriber<IScrollFunction>        scrollSubscriber;
	private final Subscriber<IMousePositionFunction> mousePositionSubscriber;
	private final Subscriber<IMouseButtonFunction>   mouseButtonSubscriber;
	private final Subscriber<IButtonFunction>        buttonSubscriber;

	public InputHandler(long windowHandleIn)
	{
		this.scrollSubscriber        = new Subscriber<>();
		this.mousePositionSubscriber = new Subscriber<>();
		this.mouseButtonSubscriber   = new Subscriber<>();
		this.buttonSubscriber        = new Subscriber<>();

		GLFW.glfwSetScrollCallback(windowHandleIn, (windowHandle, xOffset, yOffset) ->
		{
			this.scrollSubscriber.forEach(subscriber -> subscriber.onScroll(windowHandle, xOffset, yOffset));
		});

		GLFW.glfwSetCursorPosCallback(windowHandleIn, (windowHandle, xPos, yPos) ->
		{
			this.mousePositionSubscriber.forEach(subscriber -> subscriber.onMousePosition(windowHandle, xPos, yPos));

			this.mouseX = xPos;
			this.mouseY = yPos;
		});

		GLFW.glfwSetMouseButtonCallback(windowHandleIn, (windowHandle, button, action, mods) ->
		{
			for (var subscriber : this.mouseButtonSubscriber)
			{
				if (subscriber.onMouseButton(windowHandle, button, action, mods))
				{
					return;
				}
			}
		});

		GLFW.glfwSetKeyCallback(windowHandleIn, (windowHandle, key, scancode, action, mods) ->
		{
			if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_PRESS)
			{
				GLFW.glfwSetWindowShouldClose(windowHandle, true);
				return;
			}

			for (var subscriber : this.buttonSubscriber)
			{
				if (subscriber.onButton(windowHandle, key, scancode, action, mods))
				{
					return;
				}
			}
		});
	}

	public ISubscriptable<IScrollFunction> addScrollSubscriber(IScrollFunction functionIn, Priority priorityIn)
	{
		return this.scrollSubscriber.subscribe(functionIn, priorityIn);
	}

	public ISubscriptable<IScrollFunction> addScrollSubscriber(IScrollFunction functionIn)
	{
		return this.scrollSubscriber.subscribe(functionIn);
	}

	public ISubscriptable<IMousePositionFunction> addMousePositionSubscriber(IMousePositionFunction functionIn)
	{
		return this.mousePositionSubscriber.subscribe(functionIn);
	}

	public ISubscriptable<IMouseButtonFunction> addMouseButtonSubscriber(IMouseButtonFunction functionIn,
	                                                                     Priority priorityIn)
	{
		return this.mouseButtonSubscriber.subscribe(functionIn, priorityIn);
	}

	public ISubscriptable<IMouseButtonFunction> addMouseButtonSubscriber(IMouseButtonFunction functionIn)
	{
		return this.mouseButtonSubscriber.subscribe(functionIn);
	}

	public ISubscriptable<IButtonFunction> addButtonSubscriber(IButtonFunction functionIn, Priority priorityIn)
	{
		return this.buttonSubscriber.subscribe(functionIn, priorityIn);
	}

	public ISubscriptable<IButtonFunction> addButtonSubscriber(IButtonFunction functionIn)
	{
		return this.buttonSubscriber.subscribe(functionIn);
	}

	public interface IScrollFunction
	{
		void onScroll(long windowHandleIn, double xOffsetIn, double yOffsetIn);
	}

	public interface IMousePositionFunction
	{
		void onMousePosition(long windowHandleIn, double xPosIn, double yPosIn);
	}

	public interface IMouseButtonFunction
	{
		boolean onMouseButton(long windowHandleIn, int buttonIn, int actionIn, int modsIn);
	}

	public interface IButtonFunction
	{
		boolean onButton(long windowHandleIn, int keyIn, int scancodeIn, int actionIn, int modsIn);
	}
}