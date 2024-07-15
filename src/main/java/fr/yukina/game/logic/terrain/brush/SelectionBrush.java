package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.action.Action;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import fr.yukina.game.logic.terrain.picker.MousePicker;
import fr.yukina.game.utils.IOFunction;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectionBrush extends Brush
{
	private final @Getter Map<Float, List<Float>> selectedPoints;
	private @Getter       int                     selectedCount;

	public SelectionBrush(String nameIn)
	{
		super(nameIn, null);

		this.selectedPoints = new HashMap<>();
		this.selectedCount  = 0;
	}

	public final void select(float xIn, float zIn)
	{
		var list = this.selectedPoints.computeIfAbsent(xIn, (_xIn) -> new ArrayList<>());
		if (!list.contains(zIn))
		{
			list.add(zIn);
			this.selectedCount += 1;
		}
	}

	public final boolean hasSelectedPoints()
	{
		return this.selectedPoints.size() > 0 && this.selectedCount > 0;
	}

	public final void reset()
	{
		this.selectedPoints.clear();
		this.selectedCount = 0;
	}

	@Override
	public boolean update(Camera cameraIn, MousePicker mousePickerIn)
	{
		return false;
	}

	public boolean apply(Terrain terrainIn, IOFunction<Boolean> canContinueIn, Action actionIn,
	                     IHeightCollector heightCollectorIn)
	{
		if (!this.hasSelectedPoints())
		{
			return false;
		}

		for (var entry : this.selectedPoints.entrySet())
		{
			var x    = entry.getKey();
			var list = entry.getValue();
			for (var z : list)
			{
				actionIn.apply(terrainIn, x, terrainIn.get(x, z), z, heightCollectorIn);
			}
		}

		return true;
	}
}