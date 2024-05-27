package fr.yukina.game.window;

public interface IWindow
{
	void initialize();

	void pollEvents();

	void swapBuffers();

	boolean shouldClose();

	void cleanup();

	int width();

	int height();

	String title();

	int framerate();

	int synchronization();

	long handle();
}