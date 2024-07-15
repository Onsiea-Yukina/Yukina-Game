package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.action.Action;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import fr.yukina.game.utils.IOFunction;

public interface IBrushFunction
{
	boolean apply(Terrain terrainIn, IOFunction<Boolean> canContinueIn, Action actionIn, float centerXIn,
	              float centerYIn, float centerZIn, IHeightCollector heightCollectorIn);
}