package fr.yukina;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL46.GL_PATCH_VERTICES;
import static org.lwjgl.opengl.GL46.glPatchParameteri;

public class TerrainRenderer
{
	public static final int RENDER_DISTANCE = 256;

	private Matrix4f            projectionMatrix;
	private FrustumIntersection frustumIntersection = new FrustumIntersection();
	private int                 shaderProgram;
	private ChunkManager        chunkManager;

	public TerrainRenderer(ChunkManager chunkManager, int windowWidth, int windowHeight)
	{
		this.chunkManager = chunkManager;
		initMatrices(windowWidth, windowHeight);
		initShaders();
	}

	private void initShaders()
	{
		shaderProgram = ShaderLoader.loadShader("resources/shaders/terrainVertex.glsl",
		                                        "resources/shaders/terrainFragment.glsl",
		                                        "resources/shaders/terrainTessControl.glsl",
		                                        "resources/shaders/terrainTessEval.glsl");
		glUseProgram(shaderProgram);

		glUniform1i(glGetUniformLocation(shaderProgram, "textures"), 0);

		glUseProgram(0);
	}

	private void initMatrices(int windowWidth, int windowHeight)
	{
		projectionMatrix = new Matrix4f().perspective((float) Math.toRadians(90.0f),
		                                              (float) windowWidth / windowHeight,
		                                              0.1f, 4096.0f);
	}

	public void render(Camera camera)
	{
		Matrix4f viewMatrix  = camera.getViewMatrix();
		Matrix4f comboMatrix = new Matrix4f();
		projectionMatrix.mul(viewMatrix, comboMatrix);
		frustumIntersection.set(comboMatrix);

		glUseProgram(shaderProgram);
		Utils.checkGLError("After glUseProgram");

		// Set shader uniforms (projection matrix, view matrix, etc.)
		int projectionMatrixLocation = glGetUniformLocation(shaderProgram, "projectionMatrix");
		if (projectionMatrixLocation == -1)
		{
			System.err.println("ERROR::SHADER::UNIFORM::projectionMatrix not found");
		}
		else
		{
			glUniformMatrix4fv(projectionMatrixLocation, false, projectionMatrix.get(new float[16]));
			Utils.checkGLError("After setting projectionMatrix uniform");
		}

		int viewMatrixLocation = glGetUniformLocation(shaderProgram, "viewMatrix");
		if (viewMatrixLocation == -1)
		{
			System.err.println("ERROR::SHADER::UNIFORM::viewMatrix not found");
		}
		else
		{
			glUniformMatrix4fv(viewMatrixLocation, false, viewMatrix.get(new float[16]));
			Utils.checkGLError("After setting viewMatrix uniform");
		}

		// Pass camera position to the shader
		int cameraPosLocation = glGetUniformLocation(shaderProgram, "cameraPos");
		if (cameraPosLocation == -1)
		{
			System.err.println("ERROR::SHADER::UNIFORM::cameraPos not found");
		}
		else
		{
			glUniform3f(cameraPosLocation, camera.position().x, camera.position().y, camera.position().z);
			Utils.checkGLError("After setting cameraPos uniform");
		}

		// Ensure tessellation primitive type is set correctly
		glPatchParameteri(GL_PATCH_VERTICES, 3);
		Utils.checkGLError("After setting glPatchParameteri");

		// Enable wireframe mode for visualization
		glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);

		// Render chunks managed by ChunkManager
		chunkManager.renderChunks(frustumIntersection);
		Utils.checkGLError("After chunkManager.renderChunks");

		// Disable wireframe mode
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

		glUseProgram(0);
		Utils.checkGLError("After glUseProgram(0)");
	}
}