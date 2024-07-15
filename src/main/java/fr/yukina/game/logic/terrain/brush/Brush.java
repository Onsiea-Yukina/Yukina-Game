package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.action.Action;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import fr.yukina.game.logic.terrain.picker.MousePicker;
import fr.yukina.game.utils.IOFunction;
import lombok.Getter;
import lombok.Setter;

@Getter
public abstract class Brush
{
	private final           String         name;
	protected final         IBrushFunction brushFunction;
	protected @Setter       float          x;
	protected @Setter       float          y;
	protected @Setter       float          z;
	protected @Getter       boolean        hasChanged;
	private @Getter @Setter boolean        isConsistent;

	public Brush(String nameIn, IBrushFunction brushFunctionIn)
	{
		this.name          = nameIn;
		this.brushFunction = brushFunctionIn;
		this.isConsistent  = true;
	}

	public abstract boolean update(Camera cameraIn, MousePicker mousePickerIn);

	public boolean apply(Terrain terrainIn, IOFunction<Boolean> canContinueIn, Action actionIn,
	                     IHeightCollector heightCollectorIn)
	{
		if (this.brushFunction == null)
		{
			return false;
		}

		return this.brushFunction.apply(terrainIn, canContinueIn, actionIn, this.x, this.y, this.z, heightCollectorIn);
	}
}