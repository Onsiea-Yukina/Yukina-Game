package fr.yukina.game;

import fr.yukina.game.graphic.Renderer;
import fr.yukina.game.graphic.monitor.Monitoring;
import fr.yukina.game.graphic.opengl.shader.ShaderManager;
import fr.yukina.game.graphic.opengl.shader.ShaderProgram;
import fr.yukina.game.graphic.terrain.TerrainEditorRenderer;
import fr.yukina.game.graphic.terrain.TerrainRenderer;
import fr.yukina.game.graphic.window.GLFWContext;
import fr.yukina.game.logic.scene.Scene;
import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.TerrainEditor;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32;

import java.util.Scanner;

public class Main
{
	public final static Scanner SCANNER = new Scanner(System.in);
	private static      boolean isRunning;

	private static Scene         scene;
	private static Renderer      renderer;
	private static ShaderProgram cubeShaderProgram;
	private static ShaderProgram cube2ShaderProgram;
	private static ShaderProgram basicShaderProgram;

	public static void main(String[] args) throws InterruptedException
	{
		try
		{
			var glfwContext  = new GLFWContext();
			var monitors     = glfwContext.sortLargestMonitors(true, 2);
			var antialiasing = true;
			var window       = glfwContext.create(3440, 1440, "Yukina", 60, 1, antialiasing, monitors.pop());
			window.initialize();
			scene = new Scene();
			var monitoring = new Monitoring(glfwContext, antialiasing, monitors.pop(), scene.player());
			GL.createCapabilities();
			window.makeCurrent();

			var terrain         = new Terrain(0.0f, 0.0f, 0.0f);
			var terrainRenderer = new TerrainRenderer(terrain, scene.player().camera().projectionMatrix(), window);
			var terrainEditor   = new TerrainEditor(scene.player().camera(), window, terrain);
			var terrainEditorRenderer = new TerrainEditorRenderer(terrainEditor, window,
			                                                      scene.player().camera().projectionMatrix());
			GL32.glEnable(GL32.GL_DEPTH_TEST);

			monitoring.initialize();
			window.show();

			isRunning = true;
			while (!window.shouldClose() && !monitoring.shouldClose() && isRunning)
			{
				scene.update(window);
				window.makeCurrent();

				GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);

				terrainRenderer.draw(scene.player().camera());
				terrainEditor.update();
				terrainEditorRenderer.draw(scene.player().camera());
				window.swapBuffers();

				monitoring.display();
				monitoring.update();

				glfwContext.pollEvents();
			}
			terrainEditor.cleanup();
			glfwContext.destroy();
			monitoring.destroy();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			ShaderManager.stopFileWatcher();
		}
	}
}