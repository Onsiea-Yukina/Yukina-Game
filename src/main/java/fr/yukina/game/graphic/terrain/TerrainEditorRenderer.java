package fr.yukina.game.graphic.terrain;

import fr.yukina.game.graphic.opengl.nanovg.NanoVGContext;
import fr.yukina.game.graphic.opengl.nanovg.ui.CircularButton;
import fr.yukina.game.graphic.opengl.nanovg.ui.HotBar;
import fr.yukina.game.graphic.opengl.shader.ShaderProgram;
import fr.yukina.game.graphic.terrain.visualiser.TerrainSelectionVisualiser;
import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.graphic.window.handler.Priority;
import fr.yukina.game.logic.loaders.obj.OBJLoader;
import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.TerrainEditor;
import fr.yukina.game.utils.Maths;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.nanovg.NanoVG;
import org.lwjgl.opengl.GL32;

import java.io.File;

// Brush | Action | Mode | Parameters UI
public class TerrainEditorRenderer
{
	private final   TerrainEditor terrainEditor;
	private final   GLFWWindow    window;
	private @Getter NanoVGContext nanoVGContext;

	private final HotBar         hotBar;
	private final HotBar         brushsHotBar;
	private       boolean        hotBarTogglePressed;
	private final CircularButton cancelSelectionButton;

	private final TerrainSelectionVisualiser terrainSelectionVisualiser;

	private boolean leftClick;

	private       int           brushVisualisation;
	private       int           brushVertexCount;
	private       int           cubeVao;
	private final ShaderProgram shaderProgram;

	public TerrainEditorRenderer(TerrainEditor terrainEditorIn, GLFWWindow windowIn, Matrix4f projectionMatrixIn)
	{
		this.terrainEditor = terrainEditorIn;
		this.window        = windowIn;

		this.brushVisualisation = -1;
		var data     = OBJLoader.loadOBJ("resources/models/sphere.obj");
		var vertices = data.vertices();
		var normals  = data.normals();
		var indices  = data.indices();
		this.brushVertexCount = data.indices().length;
		this.cubeVao          = GL32.glGenVertexArrays();
		GL32.glBindVertexArray(this.cubeVao);
		GL32.glEnableVertexAttribArray(0);
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, GL32.glGenBuffers());
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, vertices, GL32.GL_STATIC_DRAW);
		GL32.glVertexAttribPointer(0, 3, GL32.GL_FLOAT, false, 0, 0);

		GL32.glEnableVertexAttribArray(1);
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, GL32.glGenBuffers());
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, normals, GL32.GL_STATIC_DRAW);
		GL32.glVertexAttribPointer(1, 3, GL32.GL_FLOAT, false, 0, 0);

		GL32.glBindBuffer(GL32.GL_ELEMENT_ARRAY_BUFFER, GL32.glGenBuffers());
		GL32.glBufferData(GL32.GL_ELEMENT_ARRAY_BUFFER, indices, GL32.GL_STATIC_DRAW);
		GL32.glBindVertexArray(0);

		this.nanoVGContext = new NanoVGContext(true);
		this.nanoVGContext.font("arial", "resources/fonts/arial/arial.ttf");

		this.hotBar       = new HotBar(this.nanoVGContext, windowIn.inputHandler(), 3440.0f, 1440, 100.0f, 100.0f);
		this.brushsHotBar = new HotBar(this.nanoVGContext, windowIn.inputHandler(), 3440.0f, 1440 - 110, 100.0f,
		                               200.0f);
		this.brushsHotBar.disable();

		var actionLogoFiles = new File("resources/textures/terrain/action/").listFiles();
		int i               = 0;
		for (var file : actionLogoFiles)
		{
			if (file.getName().endsWith(".png"))
			{
				var name      = file.getName().replace(".png", "");
				int logoImage = NanoVG.nvgCreateImage(this.handle(), file.getAbsolutePath(), NanoVG.NVG_IMAGE_NEAREST);
				if (logoImage == 0)
				{
					throw new RuntimeException("Could not load logo image.");
				}
				var circularButton = new CircularButton(this.nanoVGContext, this.window.inputHandler(), 0.0f, 0.0f,
				                                        50.0f, logoImage, (buttonIn) ->
				                                        {
					                                        this.terrainEditor.actionName(name);
				                                        });
				circularButton.shortcut(i == 10 ? GLFW.GLFW_KEY_0 : GLFW.GLFW_KEY_1 + i);
				this.hotBar.add(name, circularButton);
				i++;
			}
		}
		this.terrainEditor.actionName(this.hotBar.selected());

		int logoImage = NanoVG.nvgCreateImage(this.handle(), "resources/textures/terrain/cancel.png",
		                                      NanoVG.NVG_IMAGE_NEAREST);
		if (logoImage == 0)
		{
			throw new RuntimeException("Could not load logo image.");
		}
		this.cancelSelectionButton = new CircularButton(this.nanoVGContext, this.window.inputHandler(), 0.0f, 0.0f,
		                                                25.0f, logoImage, (buttonIn) ->
		                                                {
			                                                this.terrainEditor.clearSelection();
			                                                buttonIn.unselect();
		                                                });
		this.cancelSelectionButton.shortcut(GLFW.GLFW_KEY_C);

		var brushLogoFiles = new File("resources/textures/terrain/brush/").listFiles();
		i = 0;
		for (var file : brushLogoFiles)
		{
			if (file.getName().endsWith(".png"))
			{
				var name = file.getName().replace(".png", "");
				logoImage = NanoVG.nvgCreateImage(this.handle(), file.getAbsolutePath(), NanoVG.NVG_IMAGE_NEAREST);
				if (logoImage == 0)
				{
					throw new RuntimeException("Could not load logo image.");
				}
				var circularButton = new CircularButton(this.nanoVGContext, this.window.inputHandler(), 0.0f, 0.0f,
				                                        50.0f, logoImage, (buttonIn) ->
				                                        {
					                                        this.brushVisualisation = name.contentEquals("cube")
					                                                                  ? this.cubeVao
					                                                                  : -1;
					                                        this.terrainEditor.brushName(name);
				                                        });
				circularButton.shortcut(i == 10 ? GLFW.GLFW_KEY_0 : GLFW.GLFW_KEY_1 + i);
				this.brushsHotBar.add(name, circularButton);
				i++;
			}
		}
		this.terrainEditor.actionName(this.hotBar.selected());

		this.terrainSelectionVisualiser = new TerrainSelectionVisualiser(this.terrainEditor, projectionMatrixIn);

		windowIn.inputHandler().addMouseButtonSubscriber((handleIn, buttonIn, actionIn, modsIn) ->
		                                                 {
			                                                 if (this.leftClick && buttonIn == GLFW.GLFW_MOUSE_BUTTON_1
			                                                     && actionIn == GLFW.GLFW_RELEASE)
			                                                 {
				                                                 this.leftClick = false;
				                                                 return false;
			                                                 }

			                                                 if (!this.leftClick && (
					                                                 buttonIn != GLFW.GLFW_MOUSE_BUTTON_1
					                                                 || actionIn != GLFW.GLFW_PRESS))
			                                                 {
				                                                 return false;
			                                                 }
			                                                 this.leftClick = true;

			                                                 if (!this.hotBar.hovered())
			                                                 {
				                                                 this.terrainEditor.apply(this.terrainEditor.action(),
				                                                                          this.terrainEditor.terrain());

				                                                 return true;
			                                                 }

			                                                 return false;
		                                                 }, Priority.LOW);

		this.shaderProgram = new ShaderProgram.ShaderProgramBuilder(true).vertex("resources/shaders/terrain.vert")
		                                                                 .fragment("resources/shaders/terrain.frag")
		                                                                 .build();
		this.shaderProgram.attach();
		this.shaderProgram.uniform("alpha", 0.125f);
		this.shaderProgram.uniform("projection", projectionMatrixIn);
		this.shaderProgram.uniform("ambientLight", new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		// Set the directional light uniform
		this.shaderProgram.uniform("directionalLight.color", new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
		this.shaderProgram.uniform("directionalLight.power", 1.0f);
		this.shaderProgram.uniform("directionalLight.direction",
		                           new Vector3f((float) Math.toRadians(45.0f), (float) Math.toRadians(45.0f), 0.0f));

		// Set the fog uniform
		this.shaderProgram.uniform("fog.color", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
		this.shaderProgram.uniform("fog.constant", 0.025f);
		this.shaderProgram.uniform("fog.linear", 0.0000125f);
		this.shaderProgram.uniform("fog.exponent", 0.000000125f);
		this.shaderProgram.uniform("fog.enabled", 0);
		this.shaderProgram.detach();
	}

	public final void draw(Camera cameraIn)
	{
		if (this.leftClick && !this.hotBar.hovered())
		{
			this.terrainEditor.apply(this.terrainEditor.action(), this.terrainEditor.terrain());
		}
		if (this.window.pressed(GLFW.GLFW_KEY_C))
		{
			this.terrainEditor.clearSelection();
		}
		var action = GLFW.glfwGetKey(this.window.handle(), GLFW.GLFW_KEY_TAB);
		if (action == GLFW.GLFW_PRESS && !this.hotBarTogglePressed)
		{
			if (this.brushsHotBar.enabled())
			{
				this.brushsHotBar.disable();
				this.hotBar.enable();
			}
			else
			{
				this.brushsHotBar.enable();
				this.hotBar.disable();
			}

			this.hotBarTogglePressed = !this.hotBarTogglePressed;
		}
		else if (action == GLFW.GLFW_RELEASE)
		{
			this.hotBarTogglePressed = false;
		}

		this.terrainSelectionVisualiser.draw(cameraIn.viewMatrix());
		if (this.terrainEditor.mousePicker().hasTerrainPoint() && this.terrainEditor.brush() != null
		    && this.terrainEditor.action() != null)
		{
			if (this.brushVisualisation != -1)
			{
				GL32.glEnable(GL32.GL_DEPTH_TEST);
				GL32.glEnable(GL32.GL_BLEND);
				GL32.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
				GL32.glEnable(GL32.GL_CULL_FACE);
				GL32.glCullFace(GL32.GL_FRONT);
				this.shaderProgram.attach();
				this.shaderProgram.uniform("view", cameraIn.viewMatrix());
				this.shaderProgram.uniform("viewPos", cameraIn.position());
				this.shaderProgram.uniform("grid.enabled", 0);
				this.shaderProgram.uniform("model", Maths.createTransformationMatrix(
						this.terrainEditor.mousePicker().currentTerrainPoint().x,
						this.terrainEditor.mousePicker().currentTerrainPoint().y,
						this.terrainEditor.mousePicker().currentTerrainPoint().z, 0.0f, 0.0f, 0.0f, 8.0f, 8.0f, 8.0f));
				GL32.glBindVertexArray(this.brushVisualisation);
				GL32.glEnableVertexAttribArray(0);
				GL32.glEnableVertexAttribArray(1);
				GL32.glDrawElements(GL32.GL_TRIANGLES, this.brushVertexCount, GL32.GL_UNSIGNED_INT, 0);
				GL32.glDisableVertexAttribArray(1);
				GL32.glDisableVertexAttribArray(0);
				GL32.glBindVertexArray(0);
				this.shaderProgram.detach();
			}
		}

		GL32.glDisable(GL32.GL_DEPTH_TEST);
		GL32.glEnable(GL32.GL_BLEND);
		GL32.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);

		NanoVG.nvgBeginFrame(this.handle(), 3440, 1440, 1.0f);

		this.brushsHotBar.draw((indexIn, xIn, yIn, buttonNameIn, buttonIn) ->
		                       {

		                       });
		this.hotBar.draw((indexIn, xIn, yIn, buttonNameIn, buttonIn) ->
		                 {
			                 var bounds = new float[4];
			                 NanoVG.nvgFontFace(this.nanoVGContext.handle(), "arial");
			                 NanoVG.nvgFontSize(this.nanoVGContext.handle(), 32.0f);
			                 NanoVG.nvgTextAlign(this.nanoVGContext.handle(),
			                                     NanoVG.NVG_ALIGN_RIGHT | NanoVG.NVG_ALIGN_TOP);
			                 if (buttonNameIn.contentEquals("select") && this.terrainEditor.hasSelectedPoints())
			                 {
				                 this.cancelSelectionButton.x(xIn - 50);
				                 this.cancelSelectionButton.y(yIn - 50);
				                 this.cancelSelectionButton.draw();
				                 this.nanoVGContext.color().r(0.0f);
				                 this.nanoVGContext.color().g(1.0f);
				                 this.nanoVGContext.color().b(0.0f);
				                 this.nanoVGContext.color().a(1.0f);
				                 NanoVG.nvgFillColor(this.nanoVGContext.handle(), this.nanoVGContext.color());
				                 NanoVG.nvgTextBounds(this.nanoVGContext.handle(), 0.0f, 0.0f, "c", bounds);
				                 NanoVG.nvgText(this.nanoVGContext.handle(), (xIn - 35 - 50) + (bounds[2] - bounds[0]),
				                                (yIn - 35 - 50) - (bounds[3] - bounds[1]) / 2, "c");
			                 }

			                 this.nanoVGContext.color().r(0.0f);
			                 this.nanoVGContext.color().g(1.0f);
			                 this.nanoVGContext.color().b(0.0f);
			                 this.nanoVGContext.color().a(1.0f);
			                 NanoVG.nvgFillColor(this.nanoVGContext.handle(), this.nanoVGContext.color());
			                 NanoVG.nvgTextBounds(this.nanoVGContext.handle(), 0.0f, 0.0f, "" + indexIn, bounds);
			                 NanoVG.nvgText(this.nanoVGContext.handle(), (xIn + 40) + (bounds[2] - bounds[0]),
			                                (yIn + 40) - (bounds[3] - bounds[1]) / 2, "" + indexIn);
		                 });

		NanoVG.nvgEndFrame(this.handle());
		NanoVG.nvgRestore(this.handle());

		GL32.glEnable(GL32.GL_DEPTH_TEST);
		GL32.glDisable(GL32.GL_BLEND);
	}

	public final long handle()
	{
		return this.nanoVGContext.handle();
	}
}