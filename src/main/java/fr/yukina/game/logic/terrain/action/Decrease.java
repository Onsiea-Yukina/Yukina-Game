package fr.yukina.game.logic.terrain.action;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import lombok.Getter;
import lombok.Setter;

public class Decrease extends Action
{
	private @Setter @Getter float increment;

	public Decrease(String nameIn)
	{
		super(nameIn);
		this.increment = 0.125f;
	}

	public final void apply(Terrain terrainIn, float xIn, float yIn, float zIn, IHeightCollector heightCollectorIn)
	{
		heightCollectorIn.collect(xIn, zIn, terrainIn.get(xIn, zIn) - this.increment);
	}
}