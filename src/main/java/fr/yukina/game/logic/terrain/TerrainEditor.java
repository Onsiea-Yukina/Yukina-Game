package fr.yukina.game.logic.terrain;

import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.action.*;
import fr.yukina.game.logic.terrain.brush.*;
import fr.yukina.game.logic.terrain.calculation.Precalculator;
import fr.yukina.game.logic.terrain.picker.MousePicker;
import fr.yukina.game.logic.terrain.picker.RayMarchingStrategy;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class TerrainEditor
{
	private         String              actionName;
	private final   Terrain             terrain;
	private final   Precalculator       cache;
	private @Setter Brush               brush;
	private final   Map<String, Brush>  brushMap;
	private         Action              action;
	private final   Map<String, Action> actionMap;
	private final   SelectionBrush      selectionBrush;
	private         MousePicker         mousePicker;
	private         int                 synchronizationId;
	private         long                last;
	private final   Camera              camera;

	public TerrainEditor(Camera cameraIn, GLFWWindow windowIn, Terrain terrainIn)
	{
		this.terrain = terrainIn;
		this.cache   = new Precalculator(this);
		this.cache.applyFinishedFunction(() ->
		                                 {
			                                 this.terrain.increaseSynchronizationId();
			                                 this.cache.precalculate();
		                                 });
		this.selectionBrush = new SelectionBrush("selection");
		this.mousePicker    = new MousePicker(cameraIn, cameraIn.projectionMatrix(), windowIn, terrainIn,
		                                      new RayMarchingStrategy());
		this.last           = System.nanoTime();
		this.camera         = cameraIn;

		this.brushMap = new HashMap<>();
		this.brushMap.put("square", new SquareBrush("square"));
		this.brushMap.put("cube", new CubeBrush("cube"));
		this.brushMap.put("circle", new CircleBrush("circle"));
		this.brushMap.put("sphere", new SphereBrush("sphere"));
		this.brushMap.put("random", new RandomBrush("random"));
		this.brushMap.put("selection", this.selectionBrush);

		this.actionMap = new HashMap<>();
		this.actionMap.put("select", new Select("select", this));
		this.actionMap.put("increase", new Increase("increase"));
		this.actionMap.put("decrease", new Decrease("decrease"));
		this.actionMap.put("smooth", new Smooth("smooth"));
		this.actionMap.put("perlin", new Perlin("perlin"));
		this.actionMap.put("randomize", new Randomize("randomize"));
		this.actionMap.put("flat", new Flat("flat"));
	}

	public final void select(Vector3f positionIn)
	{
		this.select(positionIn.x, positionIn.z);
	}

	public final void select(float xIn, float zIn)
	{
		this.selectionBrush.select(xIn, zIn);
		this.synchronizationId++;
	}

	public final int selectedCount()
	{
		return this.selectionBrush.selectedCount();
	}

	public final boolean hasSelectedPoints()
	{
		return this.selectionBrush.hasSelectedPoints();
	}

	public final Map<Float, List<Float>> selectedPoints()
	{
		return this.selectionBrush.selectedPoints();
	}

	public final void clearSelection()
	{
		this.selectionBrush.reset();
		this.synchronizationId++;
	}

	public final void update()
	{
		if (this.mousePicker.update() && this.brush != null && this.brush.update(this.camera, this.mousePicker))
		{
			if (this.action != null && this.action.canBePrecalculated())
			{
				this.cache.precalculate();
			}
		}
		this.cache.update();
	}

	public final void apply(IActionFunction actionIn, Terrain terrainIn)
	{
		if (actionIn == null)
		{
			throw new IllegalArgumentException("[ERROR] TerrainEditor : Action is null");
		}

		if (System.nanoTime() - last < 1_000_000L)
		{
			return;
		}
		this.last = System.nanoTime();

		if (this.action != null)
		{
			if (this.action.canBePrecalculated())
			{
				this.cache.apply();
			}
			else
			{
				this.brush.apply(terrainIn, () -> true, this.action,
				                 (xIn, zIn, heightIn) -> terrainIn.set(xIn, zIn, heightIn));
			}
		}
	}

	public final void actionName(String actionNameIn)
	{
		this.actionName = actionNameIn;
		this.action     = this.actionMap.get(actionNameIn);
	}

	public final void brushName(String nameIn)
	{
		this.brush = this.brushMap.get(nameIn);
	}

	public final void cleanup()
	{
		this.cache.stop();
	}
}