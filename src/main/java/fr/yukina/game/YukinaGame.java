package fr.yukina.game;

import fr.yukina.game.window.IWindow;
import fr.yukina.game.world.World;
import lombok.Getter;

@Getter
public class YukinaGame implements IGame
{
	private final IGameLogic world;
	private final Player     player;

	public YukinaGame()
	{
		var camera = new FPSCamera((float) Math.toRadians(90.0f), 1920.0f / 1080.0f, 0.1f, 10000.0f);
		this.player = new Player(camera);
		this.world  = new World(this.player);
	}

	@Override
	public void initialize()
	{
		this.player.initialize();
		this.world.initialize();
	}

	@Override
	public void input(final IWindow windowIn)
	{
		this.player.input(windowIn);
		this.world.input(windowIn);
	}

	@Override
	public void update()
	{
		this.player.update();
		this.world.update();
	}

	@Override
	public void cleanup()
	{
		this.player.cleanup();
		this.world.cleanup();
	}
}