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
		this.world.initialize();
		this.player.initialize();
	}

	@Override
	public void input(final IWindow windowIn)
	{
		this.world.input(windowIn);
		this.player.input(windowIn);
	}

	@Override
	public void update()
	{

		this.world.update();
		this.player.update();
	}

	@Override
	public void cleanup()
	{
		this.world.cleanup();
		this.player.cleanup();
	}
}