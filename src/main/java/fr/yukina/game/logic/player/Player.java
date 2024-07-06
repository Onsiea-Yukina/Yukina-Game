package fr.yukina.game.logic.player;

import fr.yukina.game.graphic.window.GLFWWindow;
import lombok.Getter;

@Getter
public class Player
{
	private final Camera camera;

	public Player(Camera cameraIn)
	{
		this.camera = cameraIn;
	}

	public final void update(GLFWWindow windowIn)
	{
		this.camera.update(windowIn);
	}
}
