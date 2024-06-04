package fr.yukina.game.world;

import fr.yukina.game.IGameLogic;
import fr.yukina.game.Player;
import fr.yukina.game.window.IWindow;
import fr.yukina.game.world.chunk.ChunkManager;
import fr.yukina.game.world.terrain.PerlinNoise;
import lombok.Getter;

@Getter
public class World implements IGameLogic
{
	private final ChunkManager chunkManager;

	public World(Player playerIn)
	{
		var perlinNoise = new PerlinNoise(254L, 6.0f, 4.0f, 1.0f, 2, 0.25f, 0.5f, 10.0f, 10.0f);
		this.chunkManager = new ChunkManager(16, perlinNoise, playerIn);
	}

	@Override
	public void initialize()
	{
	}

	@Override
	public void input(final IWindow windowIn)
	{
	}

	@Override
	public void update()
	{
		this.chunkManager.update();
	}

	@Override
	public final void cleanup()
	{
		this.chunkManager.cleanup();
	}
}