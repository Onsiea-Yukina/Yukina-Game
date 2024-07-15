package fr.yukina.game.graphic.terrain.visualiser;

import fr.yukina.game.graphic.opengl.shader.ShaderProgram;
import fr.yukina.game.logic.terrain.TerrainEditor;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;

import java.nio.FloatBuffer;

public class TerrainSelectionVisualiser
{
	private final TerrainEditor terrainEditor;
	private       int           vao; // vertex array object
	private       int[]         abo; // attributes buffer objects
	private final ShaderProgram shaderProgram;
	private       int           terrainSynchronizationId;
	private       int           terrainEditorSynchronizationId;
	private       int           vertexCount;

	public TerrainSelectionVisualiser(TerrainEditor terrainEditorIn, Matrix4f projectionMatrixIn)
	{
		this.terrainEditor = terrainEditorIn;

		this.abo = new int[1];
		this.vao = GL32.glGenVertexArrays();
		GL32.glBindVertexArray(this.vao);
		GL20.glEnableVertexAttribArray(0);

		var vbo = GL15.glGenBuffers();
		this.abo[0] = vbo;
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
		this.vertexCount = 2;
		GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertexCount * 3 * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
		GL20.glVertexAttribPointer(0, 3, GL15.GL_FLOAT, false, 0, 0);
		GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

		GL20.glDisableVertexAttribArray(0);
		GL32.glBindVertexArray(0);

		this.shaderProgram = new ShaderProgram.ShaderProgramBuilder(true).vertex(
				"resources/shaders/terrainSelectedPoints" + ".vert").fragment(
				"resources/shaders/terrainSelectedPoints.frag").build();
		this.shaderProgram.attach();
		this.shaderProgram.uniform("projection", projectionMatrixIn);
		this.shaderProgram.detach();
	}

	public final FloatBuffer vertices()
	{
		FloatBuffer vertices = BufferUtils.createFloatBuffer(this.terrainEditor.selectedCount() * 3);
		for (var selectedPointEntry : this.terrainEditor.selectedPoints().entrySet())
		{
			var x = selectedPointEntry.getKey();
			for (var z : selectedPointEntry.getValue())
			{
				vertices.put(x);
				vertices.put(this.terrainEditor.terrain().get(x, z));
				vertices.put(z);
			}
		}
		vertices.flip();

		return vertices;
	}

	public final void draw(Matrix4f viewMatrixIn)
	{
		if (this.terrainSynchronizationId != this.terrainEditor.terrain().synchronizationId()
		    || this.terrainEditor.synchronizationId() != this.terrainEditorSynchronizationId)
		{
			GL20.glBindBuffer(GL15.GL_ARRAY_BUFFER, this.abo[0]);
			if (this.vertexCount < this.terrainEditor.selectedCount())
			{
				this.vertexCount = this.terrainEditor.selectedCount();
				GL15.glBufferData(GL15.GL_ARRAY_BUFFER, this.vertexCount * 3 * Float.BYTES, GL15.GL_DYNAMIC_DRAW);
			}
			GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, vertices());
			GL20.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
			this.terrainSynchronizationId       = this.terrainEditor.terrain().synchronizationId();
			this.terrainEditorSynchronizationId = this.terrainEditor.synchronizationId();
		}

		this.shaderProgram.attach();
		this.shaderProgram.uniform("view", viewMatrixIn);
		GL32.glBindVertexArray(vao);
		GL32.glEnableVertexAttribArray(0);
		GL20.glPointSize(10.0f);
		GL20.glDrawArrays(GL11.GL_POINTS, 0, this.terrainEditor.selectedCount());
		GL20.glDisableVertexAttribArray(0);
		GL32.glBindVertexArray(0);
		this.shaderProgram.detach();
	}
}