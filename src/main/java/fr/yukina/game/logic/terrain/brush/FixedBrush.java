package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.action.Action;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import fr.yukina.game.logic.terrain.picker.MousePicker;
import fr.yukina.game.utils.IOFunction;

public abstract class FixedBrush extends Brush
{
	private final GLFWWindow window;

	public FixedBrush(String nameIn, IBrushFunction brushFunctionIn, GLFWWindow windowIn)
	{
		super(nameIn, brushFunctionIn);

		this.window = windowIn;
	}

	@Override
	public boolean update(Camera cameraIn, MousePicker mousePickerIn)
	{
		return false;
	}

	public boolean apply(Terrain terrainIn, IOFunction<Boolean> canContinueIn, Action actionIn,
	                     IHeightCollector heightCollectorIn)
	{
		return this.brushFunction.apply(terrainIn, canContinueIn, actionIn, 0.0f, 0.0f, 0.0f, heightCollectorIn);
	}
}