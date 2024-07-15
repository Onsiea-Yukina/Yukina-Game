package fr.yukina.game.logic.terrain.action;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.height.IHeightCollector;

public class Flat extends Action
{
	public Flat(String nameIn)
	{
		super(nameIn);
	}

	public final void apply(Terrain terrainIn, float xIn, float yIn, float zIn, IHeightCollector heightCollectorIn)
	{
		heightCollectorIn.collect(xIn, zIn, 0.0f);
	}
}