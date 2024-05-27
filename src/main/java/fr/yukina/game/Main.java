package fr.yukina.game;

import fr.yukina.game.render.YukinaGameRenderer;

public class Main
{
	public static void main(String[] args)
	{
		System.setProperty("org.lwjgl.util.Debug", "true");
		System.setProperty("org.lwjgl.util.DebugLoader", "true");

		new YukinaClient(1920, 1080, "Yukina", 60, 1, new YukinaGame(), new YukinaGameRenderer()).start();
	}
}
