package fr.yukina.game.graphic.opengl;

import lombok.Setter;
import org.lwjgl.opengl.GL32;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GLMesh
{
	private final   int           vao;
	private final   List<Integer> vboList;
	private         int           attribCount = 0;
	private         Integer       ibo;
	private @Setter int           count;

	public GLMesh()
	{
		this.vao     = GL32.glGenVertexArrays();
		this.vboList = new ArrayList<>();
	}

	public final GLMesh attach()
	{
		GL32.glBindVertexArray(this.vao);
		for (int i = 0; i < this.attribCount; i++)
		{
			GL32.glEnableVertexAttribArray(i);
		}
		return this;
	}

	public final GLMesh data(float[] dataIn)
	{
		if (dataIn == null || dataIn.length == 0)
		{
			throw new IllegalArgumentException("Data array is null or empty.");
		}
		var vbo = GL32.glGenBuffers();
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, vbo);
		GL32.glBufferData(GL32.GL_ARRAY_BUFFER, dataIn, GL32.GL_STATIC_DRAW);
		this.vboList.add(vbo);
		return this;
	}

	public final GLMesh upload(int sizeIn, float[] dataIn)
	{
		return this.upload(sizeIn, dataIn, GL32.GL_FLOAT, false, 0, 0);
	}

	public final GLMesh upload(int sizeIn, float[] dataIn, int typeIn, boolean normalizedIn, int strideIn,
	                           long pointerIn)
	{
		if (dataIn == null || dataIn.length == 0)
		{
			throw new IllegalArgumentException("Data array is null or empty.");
		}
		if (this.vboList.size() == 0)
		{
			this.count = dataIn.length / sizeIn;
		}

		this.data(dataIn);
		this.attrib(sizeIn, typeIn, normalizedIn, strideIn, pointerIn);
		GL32.glBindBuffer(GL32.GL_ARRAY_BUFFER, 0);

		return this;
	}

	public final GLMesh attrib(int sizeIn, int typeIn, boolean normalizedIn, int strideIn, long pointerIn)
	{
		System.out.println("Setting attribute at index: " + this.attribCount);
		GL32.glEnableVertexAttribArray(this.attribCount);
		GL32.glVertexAttribPointer(this.attribCount, sizeIn, typeIn, normalizedIn, strideIn, pointerIn);
		this.attribCount++;
		return this;
	}

	public final GLMesh indices(int[] dataIn)
	{
		if (dataIn == null || dataIn.length == 0)
		{
			throw new IllegalArgumentException("Index data array is null or empty.");
		}
		this.ibo   = GL32.glGenBuffers();
		this.count = dataIn.length;

		GL32.glBindBuffer(GL32.GL_ELEMENT_ARRAY_BUFFER, ibo);
		GL32.glBufferData(GL32.GL_ELEMENT_ARRAY_BUFFER, dataIn, GL32.GL_STATIC_DRAW);

		return this;
	}

	public final GLMesh draw()
	{
		this.attach();

		if (this.ibo != null)
		{
			GL32.glDrawElements(GL32.GL_TRIANGLES, this.count, GL32.GL_UNSIGNED_INT, 0);
			return this;
		}

		GL32.glDrawArrays(GL32.GL_TRIANGLES, 0, this.count);

		this.detach();

		return this;
	}

	public final GLMesh detach()
	{
		for (int i = this.attribCount - 1; i >= 0; i--)
		{
			GL32.glDisableVertexAttribArray(i);
		}
		GL32.glBindVertexArray(0);

		return this;
	}

	public final void cleanup()
	{
		GL32.glDeleteVertexArrays(this.vao);
		for (int vbo : this.vboList)
		{
			GL32.glDeleteBuffers(vbo);
		}
		if (this.ibo != null)
		{
			GL32.glDeleteBuffers(this.ibo);
		}
	}

	@Override
	public boolean equals(final Object oIn)
	{
		if (this == oIn)
		{
			return true;
		}
		if (oIn == null || getClass() != oIn.getClass())
		{
			return false;
		}
		final GLMesh glMesh = (GLMesh) oIn;
		return this.vao == glMesh.vao;
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(this.vao);
	}
}