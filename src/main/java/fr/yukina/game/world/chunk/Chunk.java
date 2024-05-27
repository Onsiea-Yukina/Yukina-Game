package fr.yukina.game.world.chunk;

import fr.yukina.game.world.terrain.ITerrainGenerator;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Chunk
{
	private final   String  key;
	private final   Terrain terrain;
	private @Setter boolean visible;
	private @Setter boolean needUnload;

	public Chunk(String keyIn, int xIn, int yIn, int zIn, int widthIn, int depthIn, ITerrainGenerator generatorIn)
	{
		this.key        = keyIn;
		this.terrain    = new Terrain(xIn, yIn, zIn, widthIn, depthIn, generatorIn);
		this.visible    = true;
		this.needUnload = false;
	}

	public final void generate()
	{
		this.terrain.generate();
	}

	public final void cleanup()
	{
		this.terrain.cleanup();
	}
}