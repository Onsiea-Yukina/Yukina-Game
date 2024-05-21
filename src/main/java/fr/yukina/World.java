package fr.yukina;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.glEnable;

public class World
{
	private long            window;
	private Camera          camera;
	private ChunkManager    chunkManager;
	private TerrainRenderer terrainRenderer;

	public World(long window, int width, int height)
	{
		this.window = window;
		init(width, height);
	}

	private void init(int width, int height)
	{
		camera = new Camera(new Vector3f(0.0f, 5.0f, 0.0f));

		PerlinNoise perlinNoise = new PerlinNoise(System.currentTimeMillis(), 8, 0.5f, 4.0f, 10.0f); // Configurable
		// parameters
		int textureArrayId = TextureArrayLoader.loadTextureArray(
				List.of("resources/textures/dirt.png", "resources/textures/stone.png"));

		chunkManager    = new ChunkManager(textureArrayId, perlinNoise, camera);
		terrainRenderer = new TerrainRenderer(chunkManager, width, height);

		GL.createCapabilities();
		glEnable(GL_DEPTH_TEST);
	}

	public void update(long window)
	{
		camera.processInput(window);

		double[] xpos = new double[1];
		double[] ypos = new double[1];
		GLFW.glfwGetCursorPos(window, xpos, ypos);
		GLFW.glfwSetCursorPos(window, 400, 300);

		double xoffset = xpos[0] - 400;
		double yoffset = 300 - ypos[0];

		camera.processMouseInput(xoffset, yoffset);

		chunkManager.update();
	}

	public void render()
	{
		terrainRenderer.render(camera);
	}

	public Camera getCamera()
	{
		return camera;
	}
}