package fr.yukina;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL46.GL_PATCHES;

@Getter
@Setter
public class Chunk
{
	private int x, z;
	private int         vaoId;
	private int         vboId;
	private int         eboId;
	private int         vertexCount;
	private int         indexCount;
	private int         textureArrayId;
	private PerlinNoise perlinNoise;

	public Chunk(int x, int z, int textureArrayId, PerlinNoise perlinNoise)
	{
		this.x              = x;
		this.z              = z;
		this.textureArrayId = textureArrayId;
		this.perlinNoise    = perlinNoise;
		this.generateTerrain();
	}

	private void generateTerrain()
	{
		FloatBuffer vertices = this.createTerrainVertices();
		IntBuffer   indices  = this.createTerrainIndices();
		this.vertexCount = vertices.limit() / 6; // 3 for position + 2 for texture coords + 1 for texture index
		this.indexCount  = indices.limit();

		this.vaoId = glGenVertexArrays();
		glBindVertexArray(vaoId);

		this.vboId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboId);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

		this.eboId = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboId);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 3, GL_FLOAT, false, 6 * Float.BYTES, 0);
		glEnableVertexAttribArray(0);

		glVertexAttribPointer(1, 2, GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
		glEnableVertexAttribArray(1);

		glVertexAttribPointer(2, 1, GL_FLOAT, false, 6 * Float.BYTES, 5 * Float.BYTES);
		glEnableVertexAttribArray(2);

		glBindVertexArray(0);
	}

	private FloatBuffer createTerrainVertices()
	{
		int     size     = 16; // Size of the chunk
		float[] vertices = new float[(size + 1) * (size + 1) * 6];
		int     index    = 0;
		double  scale    = 0.1;

		for (int i = 0; i <= size; i++)
		{
			for (int j = 0; j <= size; j++)
			{
				float height = (float) perlinNoise.getNoise((x + i) * scale, 0, (z + j) * scale);
				vertices[index++] = x + i; // x position
				vertices[index++] = height; // y position (height from Perlin noise)
				vertices[index++] = z + j; // z position
				vertices[index++] = (float) i / size; // texture coord x
				vertices[index++] = (float) j / size; // texture coord y
				vertices[index++] = (i + j) % 2; // texture index (example: alternating textures)
			}
		}

		return BufferUtils.createFloatBuffer(vertices.length).put(vertices).flip();
	}

	private IntBuffer createTerrainIndices()
	{
		int   size    = 16; // Size of the chunk
		int[] indices = new int[size * size * 6];
		int   index   = 0;

		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				int topLeft     = (i * (size + 1)) + j;
				int topRight    = topLeft + 1;
				int bottomLeft  = ((i + 1) * (size + 1)) + j;
				int bottomRight = bottomLeft + 1;

				indices[index++] = topLeft;
				indices[index++] = bottomLeft;
				indices[index++] = topRight;
				indices[index++] = topRight;
				indices[index++] = bottomLeft;
				indices[index++] = bottomRight;
			}
		}

		return BufferUtils.createIntBuffer(indices.length).put(indices).flip();
	}

	public void render()
	{
		glBindTexture(GL_TEXTURE_2D_ARRAY, textureArrayId);

		glBindVertexArray(vaoId);
		glDrawElements(GL_PATCHES, indexCount, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);
	}
}