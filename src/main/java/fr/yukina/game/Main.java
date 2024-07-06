package fr.yukina.game;

import fr.yukina.game.graphic.Renderer;
import fr.yukina.game.graphic.monitor.Monitoring;
import fr.yukina.game.graphic.opengl.FBO;
import fr.yukina.game.graphic.opengl.GLMesh;
import fr.yukina.game.graphic.opengl.shader.ShaderManager;
import fr.yukina.game.graphic.opengl.shader.ShaderProgram;
import fr.yukina.game.graphic.window.GLFWContext;
import fr.yukina.game.logic.entity.Entity;
import fr.yukina.game.logic.items.GameItem;
import fr.yukina.game.logic.items.Material;
import fr.yukina.game.logic.loaders.obj.OBJLoader;
import fr.yukina.game.logic.loaders.obj.OBJUtils;
import fr.yukina.game.logic.player.Player;
import fr.yukina.game.logic.scene.Scene;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import java.util.Scanner;

import static org.lwjgl.opengl.GL30.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL30.glEnable;

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

			var cubeMesh = OBJUtils.toMesh(OBJLoader.loadOBJ("resources/models/cube.obj"));

			for (var x = 0; x < 1; x++)
			{
				for (var y = 0; y < 1; y++)
				{
					//var x            = (float) Math.random() * 500 - 250;
					//var y            = (float) Math.random() * 500 - 250;
					var z            = 0.0f;
					var xOrientation = 0.0f; //(float) Math.random() * 360;
					var yOrientation = 0.0f; //(float) Math.random() * 360;
					var zOrientation = 0.0f; //(float) Math.random() * 360;
					var xScale       = 1.0f;
					var yScale       = 1.0f;
					var zScale       = 1.0f;
					var material     = new Material(32.0f);
					material.color().set(0.125f, 0.125f, 0.125f, 0.125f);
					material.diffuseColor().set(1.0f, 0.0f, 1.0f, 1.0f);
					material.specularColor().set(1.0f, 0.25f, 0.0125f, 1.0f);
					var cube = new GameItem(material, x, y, z, xOrientation, yOrientation, zOrientation, xScale,
					                        yScale,
					                        zScale);
					var entity = new Entity(cube);
				/*entity.acceleration().set((float) Math.random() * 0.01f, -0.98f / 60.0f,
				                          (float) Math.random() * 0.01f);
				entity.orientationAcceleration()
				      .set((float) Math.random() * 0.01f, (float) Math.random() * 0.01f,
				           (float) Math.random() * 0.01f);*/
					entity.velocity().set(0.0f, 0.0f, 0.0f);
					scene.add(cubeMesh, entity);
				}
			}
			renderer = new Renderer(scene);

			var shaderManager = new ShaderManager();
			cubeShaderProgram  = new ShaderProgram.ShaderProgramBuilder(false).vertex("resources/shaders/cube.vert")
			                                                                  .fragment("resources/shaders/cube.frag")
			                                                                  .build();
			cube2ShaderProgram = new ShaderProgram.ShaderProgramBuilder(false).vertex("resources/shaders/cube2.vert")
			                                                                  .fragment("resources/shaders/cube2.frag")
			                                                                  .build();
			basicShaderProgram = new ShaderProgram.ShaderProgramBuilder(false).vertex("resources/shaders/basic.vert")
			                                                                  .fragment("resources/shaders/basic.frag")
			                                                                  .build();
			shaderManager.add("cube", cubeShaderProgram);
			shaderManager.add("cube2", cube2ShaderProgram);
			shaderManager.add("basic", basicShaderProgram);
			float[] rectangleVertices = { -1.0f, -1.0f, 1.0f, -1.0f, 1.0f, 1.0f, -1.0f, 1.0f,
			                              };
			int[] rectangleIndices = { 0, 1, 2, 2, 3, 0 };
			var   rectangleMesh    = new GLMesh();
			rectangleMesh.attach();
			rectangleMesh.upload(2, rectangleVertices);
			rectangleMesh.indices(rectangleIndices);
			rectangleMesh.detach();

			var fbo = new FBO(window.width(), window.height(), window);

			monitoring.initialize();
			window.show();

			isRunning = true;
			while (!window.shouldClose() && !monitoring.shouldClose() && isRunning)
			{
				scene.update(window);

				window.makeCurrent();
				shaderManager.update();
				GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);

				/*{ // cube shader
					fbo.start();
					render(scene.player(), cubeShaderProgram);
					fbo.stop(window);
					GL32.glViewport(0, 0, 3440 / 2, 1440 / 2);
					basicShaderProgram.attach();
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.colourTexture());
					rectangleMesh.draw();
				}*/

				{ // cube2 shader
					fbo.start();
					render(scene.player(), cube2ShaderProgram);
					fbo.stop(window);
					GL32.glViewport(0, 0, 3440, 1440);
					basicShaderProgram.attach();
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbo.colourTexture());
					rectangleMesh.draw();
				}

				basicShaderProgram.detach();

				window.swapBuffers();

				monitoring.display();
				monitoring.update();

				glfwContext.pollEvents();
			}
			shaderManager.cleanup();
			cleanup();
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

	private static void render(Player playerIn, ShaderProgram shaderProgramIn)
	{
		GL32.glViewport(0, 0, 3440, 1440);
		GL32.glClear(GL32.GL_COLOR_BUFFER_BIT | GL32.GL_DEPTH_BUFFER_BIT | GL32.GL_STENCIL_BUFFER_BIT);
		glEnable(GL_DEPTH_TEST);

		shaderProgramIn.attach();

		renderer.render(shaderProgramIn);

		shaderProgramIn.detach();
	}

	private static void cleanup()
	{
		renderer.cleanup();
	}
}