package fr.yukina.game;

import fr.yukina.game.window.IWindow;

public interface IGameLogic
{
	void initialize();

	void input(IWindow windowIn);

	void update();

	void cleanup();
}
