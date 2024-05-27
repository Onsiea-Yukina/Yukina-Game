package fr.yukina.game;

import fr.yukina.game.render.IGameRenderer;
import fr.yukina.game.window.GLFWWindow;
import fr.yukina.game.window.IWindow;

public class YukinaClient
{
	private final IWindow       window;
	private final IGame         game;
	private final IGameRenderer gameRenderer;
	private       boolean       isRunning;

	public YukinaClient(int widthIn, int heightIn, String titleIn, int framerateIn, int synchronizationIn,
	                    IGame gameIn,
	                    IGameRenderer gameRendererIn)
	{
		this.window       = new GLFWWindow(widthIn, heightIn, titleIn, framerateIn, synchronizationIn);
		this.gameRenderer = gameRendererIn;
		this.isRunning    = false;
		this.game         = gameIn;
	}

	public final void start()
	{
		this.window.initialize();
		this.game.initialize();
		this.gameRenderer.initialize(this.window, this.game);

		this.isRunning = true;
		while (this.isRunning)
		{
			if (this.window.shouldClose())
			{
				this.isRunning = false;
				break;
			}

			this.window.pollEvents();
			this.game.input(this.window);
			this.game.update();
			if (this.gameRenderer.render())
			{
				this.window.swapBuffers();
			}
		}

		this.game.cleanup();
		this.window.cleanup();
		this.gameRenderer.cleanup();
	}
}