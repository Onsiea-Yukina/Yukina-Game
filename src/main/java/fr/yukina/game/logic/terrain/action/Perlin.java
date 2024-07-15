package fr.yukina.game.logic.terrain.action;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import fr.yukina.game.logic.terrain.height.NoiseGenerator;
import lombok.Getter;
import lombok.Setter;
import org.joml.Random;

public class Perlin extends Action
{
	private @Setter @Getter float          increment;
	private final           NoiseGenerator perlinNoise;

	public Perlin(String nameIn)
	{
		super(nameIn);
		this.increment   = 0.125f;
		this.perlinNoise = new NoiseGenerator(Random.newSeed(), 6, 0.75f);
	}

	public final void apply(Terrain terrainIn, float xIn, float yIn, float zIn, IHeightCollector heightCollectorIn)
	{

		this.perlinNoise.amplitude(32.0f);
		heightCollectorIn.collect(xIn, zIn, this.perlinNoise.height((int) (xIn), (int) (zIn)));
	}
}