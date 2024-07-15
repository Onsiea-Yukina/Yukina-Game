package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.picker.MousePicker;
import org.lwjgl.glfw.GLFW;

public abstract class AnchorBrush extends Brush
{
	private final GLFWWindow window;

	public AnchorBrush(String nameIn, IBrushFunction brushFunctionIn, GLFWWindow windowIn)
	{
		super(nameIn, brushFunctionIn);

		this.window = windowIn;
	}

	@Override
	public boolean update(Camera cameraIn, MousePicker mousePickerIn)
	{
		if (GLFW.glfwGetMouseButton(this.window.handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) != GLFW.GLFW_PRESS)
		{
			return false;
		}

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
