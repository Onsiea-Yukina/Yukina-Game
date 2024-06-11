package fr.yukina.game;

import fr.yukina.game.render.YukinaGameRenderer;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main
{
	public static void main(String[] args)
	{
		System.setProperty("org.lwjgl.util.Debug", "true");
		System.setProperty("org.lwjgl.util.DebugLoader", "true");

		// Set the logging level to WARNING for the root logger
		Logger rootLogger = Logger.getLogger("");
		rootLogger.setLevel(Level.OFF);

		// Optionally, configure handlers to apply the same level
		for (Handler handler : rootLogger.getHandlers())
		{
			handler.setLevel(Level.OFF);
		}

		new YukinaClient(1920, 1080, "Yukina", 60, 1, new YukinaGame(), new YukinaGameRenderer()).start();
	}
}
