package fr.yukina.game.logic.terrain.action;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.height.IHeightCollector;

public interface IActionFunction
{
	void apply(Terrain terrainIn, float xIn, float yIn, float zIn, IHeightCollector heightCollectorIn);
}