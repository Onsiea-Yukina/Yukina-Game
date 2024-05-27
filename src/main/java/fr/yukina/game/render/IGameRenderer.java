package fr.yukina.game.render;

import fr.yukina.game.IGame;
import fr.yukina.game.window.IWindow;

public interface IGameRenderer
{
	void initialize(IWindow window, IGame game);

	boolean render();

	void cleanup();
}