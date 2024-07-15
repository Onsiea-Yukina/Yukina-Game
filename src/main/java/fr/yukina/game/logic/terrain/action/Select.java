package fr.yukina.game.logic.terrain.action;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.TerrainEditor;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import lombok.Getter;
import lombok.Setter;

public class Select extends Action
{
	private @Setter @Getter float         increment;
	private final           TerrainEditor terrainEditor;

	public Select(String nameIn, TerrainEditor terrainEditorIn)
	{
		super(nameIn);

		this.canBePrecalculated(false);
		this.terrainEditor = terrainEditorIn;
	}

	public final void apply(Terrain terrainIn, float xIn, float yIn, float zIn, IHeightCollector heightCollectorIn)
	{
		this.terrainEditor.select(xIn, zIn);
	}
}