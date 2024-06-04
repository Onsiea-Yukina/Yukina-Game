package fr.yukina.game.world.chunk;

import fr.yukina.game.world.terrain.ITerrainGenerator;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
public class Chunk
{
	private final static int HEIGHT = 16;

	private final   String   key;
	private final   Terrain  terrain;
	private @Setter boolean  visible;
	private @Setter boolean  needUnload;
	private         Vector3f min;
	private         Vector3f max;

	public Chunk(String keyIn, int xIn, int yIn, int zIn, int widthIn, int depthIn, ITerrainGenerator generatorIn)
	{
		this.key        = keyIn;
		this.terrain    = new Terrain(xIn, yIn, zIn, widthIn, depthIn, generatorIn);
		this.min        = new Vector3f(xIn, yIn, zIn);
		this.max        = new Vector3f(xIn + widthIn, yIn + HEIGHT, zIn + depthIn);
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