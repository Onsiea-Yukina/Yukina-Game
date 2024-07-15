package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.picker.MousePicker;

public abstract class CursorBrush extends Brush
{
	public CursorBrush(String nameIn, IBrushFunction brushFunctionIn)
	{
		super(nameIn, brushFunctionIn);
	}

	@Override
	public boolean update(Camera cameraIn, MousePicker mousePickerIn)
	{
		if (!mousePickerIn.hasTerrainPoint() || mousePickerIn.currentTerrainPoint() == null
		    || mousePickerIn.currentTerrainPoint().x < 0 || mousePickerIn.currentTerrainPoint().z < 0
		    || mousePickerIn.currentTerrainPoint().x > Terrain.WIDTH - 1
		    || mousePickerIn.currentTerrainPoint().z > Terrain.DEPTH - 1)
		{
			return false;
		}

		if (this.x == mousePickerIn.currentTerrainPoint().x && this.y == mousePickerIn.currentTerrainPoint().y
		    && this.z == mousePickerIn.currentTerrainPoint().z)
		{
			return false;
		}

		this.hasChanged = true;
		this.x          = mousePickerIn.currentTerrainPoint().x;
		this.y          = mousePickerIn.currentTerrainPoint().y;
		this.z          = mousePickerIn.currentTerrainPoint().z;

		return true;
	}
}
