package fr.yukina.game.graphic.terrain;

import fr.yukina.game.graphic.opengl.shader.ShaderProgram;
import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.utils.Maths;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL46;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class TerrainRenderer
{
	private final Terrain       terrain;
	private       int           vao; // vertex array object
	private       int[]         abo; // attributes buffer objects
	private       int           ibo; // indices buffer object
	private       int           vertexCount;
	private final ShaderProgram shaderProgram;
	private       int           synchronizationId;
	private       GLFWWindow    window;
	private       int           renderMode;
	private       boolean       renderModeKeyPressed;
	private       boolean       gridToggleKeyPressed;
	private       boolean       gridEnabled;
	private       float         alpha;

	public TerrainRenderer(Terrain terrainIn, Matrix4f projectionMatrixIn, GLFWWindow windowIn)
	{
		this.terrain = terrainIn;
		this.window  = windowIn;

		this.alpha = 1.0f;

		this.abo = new int[1];
		this.vao = GL32.glGenVertexArrays();
		GL32.glBindVertexArray(this.vao);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);

		var vbo = GL15.glGenBuffers();
		this.abo[0] = vbo;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertices(), GL15.GL_DYNAMIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL15.GL_FLOAT, false, 6 * Float.BYTES, 0);
		GL20.glVertexAttribPointer(1, 3, GL15.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		int[] indices = generateStripIndices(Terrain.WIDTH, Terrain.DEPTH);
		this.ibo = GL15.glGenBuffers();
		GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, this.ibo);
		IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indices.length);
		indicesBuffer.put(indices);
		indicesBuffer.flip();
		this.vertexCount = indices.length;
		GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL15.GL_DYNAMIC_DRAW);

		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL32.glBindVertexArray(0);

		this.shaderProgram = new ShaderProgram.ShaderProgramBuilder(true).vertex("resources/shaders/terrain.vert")
		                                                                 .fragment("resources/shaders/terrain.frag")
		                                                                 .build();
		this.shaderProgram.attach();
		this.shaderProgram.uniform("alpha", this.alpha);
		this.shaderProgram.uniform("projection", projectionMatrixIn);
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
		this.shaderProgram.uniform("model",
		                           Maths.createTransformationMatrix(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f,
		                                                            1.0f));
		this.shaderProgram.uniform("grid.size", new Vector2f(1.0f, 1.0f));
		this.shaderProgram.uniform("grid.offset", new Vector2f(0.0f, 0.0f));
		this.shaderProgram.uniform("grid.lineWidth", 0.025f);
		this.shaderProgram.uniform("grid.gradientWidth", 2.125f);
		this.shaderProgram.uniform("grid.weight", 0.95f);
		this.shaderProgram.uniform("grid.noisePower", 0.125f);
		this.shaderProgram.uniform("grid.color", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
		this.shaderProgram.uniform("grid.minColorThreshold", 0.5f);
		this.shaderProgram.uniform("grid.enabled", 1);
		this.shaderProgram.detach();
	}

	public final void alpha(float alphaIn)
	{
		this.alpha = alphaIn;

		this.shaderProgram.attach();
		this.shaderProgram.uniform("alpha", this.alpha);
		this.shaderProgram.detach();
	}

	private FloatBuffer vertices()
	{
		FloatBuffer vertices = BufferUtils.createFloatBuffer(Terrain.WIDTH * Terrain.DEPTH * 6);
		for (var x = 0; x < Terrain.WIDTH; x++)
		{
			for (var z = 0; z < Terrain.DEPTH; z++)
			{
				vertices.put(x + this.terrain.x());
				vertices.put(this.terrain.get(x, z) + this.terrain.y());
				vertices.put(z + this.terrain.z());

				float    heightL = x > 0 ? terrain.get(x - 1, z) : terrain.get(x, z);
				float    heightR = x < Terrain.WIDTH - 1 ? terrain.get(x + 1, z) : terrain.get(x, z);
				float    heightD = z > 0 ? terrain.get(x, z - 1) : terrain.get(x, z);
				float    heightU = z < Terrain.DEPTH - 1 ? terrain.get(x, z + 1) : terrain.get(x, z);
				Vector3f normal  = new Vector3f(heightL - heightR, 2.0f, heightD - heightU).normalize();

				vertices.put(normal.x); // nx normal
				vertices.put(normal.y); // ny normal
				vertices.put(normal.z); // nz normal
			}
		}
		vertices.flip();
		return vertices;
	}

	private int[] generateIndices(int widthIn, int depthIn)
	{
		int[] indices = new int[(widthIn - 1) * (depthIn - 1) * 6];
		int   index   = 0;
		for (int x = 0; x < widthIn - 1; x++)
		{
			for (int z = 0; z < depthIn - 1; z++)
			{
				int topLeft     = (x * depthIn) + z;
				int topRight    = topLeft + 1;
				int bottomLeft  = ((x + 1) * depthIn) + z;
				int bottomRight = bottomLeft + 1;

				// First triangle
				indices[index++] = topLeft;
				indices[index++] = bottomLeft;
				indices[index++] = topRight;

				// Second triangle
				indices[index++] = topRight;
				indices[index++] = bottomLeft;
				indices[index++] = bottomRight;
			}
		}
		return indices;
	}

	private int[] generateStripIndices(int widthIn, int depthIn)
	{
		int[] indices = new int[(depthIn - 1) * ((widthIn - 1) * 2 + 2) + ((depthIn - 1) - 1)];
		int   index   = 0;
		for (int z = 0; z < depthIn - 1; z++)
		{
			for (int x = 0; x < widthIn; x++)
			{
				indices[index++] = (z * widthIn) + x;
				indices[index++] = (z + 1) * widthIn + x;
			}
			if (z < depthIn - 2) // add degenerate vertices to break the strip at the end of the row
			{
				indices[index++] = 0xFFFFFFFF;
			}
		}
		return indices;
	}

	public final void draw(Camera cameraIn)
	{
		this.shaderProgram.attach();
		var action = GLFW.glfwGetKey(this.window.handle(), GLFW.GLFW_KEY_R);
		if (action == GLFW.GLFW_PRESS && !this.renderModeKeyPressed)
		{
			this.renderMode++;
			this.renderMode %= 3;
			this.renderModeKeyPressed = true;
		}
		else if (action == GLFW.GLFW_RELEASE)
		{
			this.renderModeKeyPressed = false;
		}
		action = GLFW.glfwGetKey(this.window.handle(), GLFW.GLFW_KEY_G);
		if (action == GLFW.GLFW_PRESS && !this.gridToggleKeyPressed)
		{
			this.gridEnabled          = !this.gridEnabled;
			this.gridToggleKeyPressed = true;
			this.shaderProgram.uniform("grid.enabled", this.gridEnabled ? 1 : 0);
		}
		else if (action == GLFW.GLFW_RELEASE)
		{
			this.gridToggleKeyPressed = false;
		}

		if (this.terrain.synchronizationId() != this.synchronizationId)
		{
			GL32.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.abo[0]);
			GL32.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices());
			this.synchronizationId = this.terrain.synchronizationId();
		}

		this.shaderProgram.uniform("view", cameraIn.viewMatrix());
		this.shaderProgram.uniform("viewPos", cameraIn.position());
		GL32.glBindVertexArray(this.vao);
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);

		switch (renderMode)
		{
			case 0:
				GL32.glPolygonMode(GL32.GL_FRONT_AND_BACK, GL32.GL_FILL);
				break;
			case 1:
				GL32.glPolygonMode(GL32.GL_FRONT_AND_BACK, GL32.GL_LINE);
				break;
			case 2:
				GL32.glPolygonMode(GL32.GL_FRONT_AND_BACK, GL32.GL_POINT);
				break;
		}

		GL32.glEnable(GL32.GL_PRIMITIVE_RESTART);
		GL32.glPrimitiveRestartIndex(0xFFFFFFFF);
		GL32.glDrawElements(GL32.GL_TRIANGLE_STRIP, this.vertexCount, GL32.GL_UNSIGNED_INT, 0L);
		GL32.glDisable(GL32.GL_PRIMITIVE_RESTART);

		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(0);
		GL32.glBindVertexArray(0);
		this.shaderProgram.detach();
		GL32.glPolygonMode(GL32.GL_FRONT_AND_BACK, GL32.GL_FILL);
	}

	private int[] generateGridIndices(int widthIn, int depthIn)
	{
		int[] indices = new int[(widthIn * (depthIn - 1) + depthIn * (widthIn - 1)) * 2];
		int   index   = 0;

		// Generate vertical lines
		for (int x = 0; x < widthIn; x++)
		{
			for (int z = 0; z < depthIn - 1; z++)
			{
				indices[index++] = (z * widthIn) + x;
				indices[index++] = ((z + 1) * widthIn) + x;
			}
		}

		// Generate horizontal lines
		for (int z = 0; z < depthIn; z++)
		{
			for (int x = 0; x < widthIn - 1; x++)
			{
				indices[index++] = (z * widthIn) + x;
				indices[index++] = (z * widthIn) + (x + 1);
			}
		}

		return indices;
	}

	public final void drawGrid(Matrix4f viewMatrixIn)
	{
		this.shaderProgram.attach();
		this.shaderProgram.uniform("view", viewMatrixIn);
		GL32.glBindVertexArray(this.vao);
		GL20.glEnableVertexAttribArray(0);

		// Enable polygon mode for lines
		GL32.glPolygonMode(GL32.GL_FRONT_AND_BACK, GL46.GL_LINE);
		GL32.glDrawElements(GL32.GL_LINES, this.vertexCount, GL32.GL_UNSIGNED_INT, 0L);
		GL32.glPolygonMode(GL32.GL_FRONT_AND_BACK, GL32.GL_FILL); // Reset to fill mode

		GL20.glDisableVertexAttribArray(0);
		GL32.glBindVertexArray(0);
		this.shaderProgram.detach();
	}
}