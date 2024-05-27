package fr.yukina.game.render.opengl;

import lombok.Getter;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

@Getter
public class VertexArrayObject
{
	private final int id;
	private       int attributesCount;

	public VertexArrayObject()
	{
		this.id = GL30.glGenVertexArrays();
	}

	public void attach()
	{
		GL30.glBindVertexArray(this.id);
		for (int i = 0; i < this.attributesCount; i++)
		{
			GL20.glEnableVertexAttribArray(i);
		}
	}

	public void attribute(int indexIn, int sizeIn, int strideIn, int offsetIn)
	{
		GL20.glEnableVertexAttribArray(indexIn);
		this.attributesCount++;

		GL20.glVertexAttribPointer(indexIn, sizeIn, GL20.GL_FLOAT, false, strideIn, offsetIn);
	}

	public void detach()
	{
		for (int i = this.attributesCount - 1; i >= 0; i--)
		{
			GL20.glDisableVertexAttribArray(i);
		}
		GL30.glBindVertexArray(0);
	}

	public void cleanup()
	{
		this.detach();
		GL30.glDeleteVertexArrays(this.id);
	}
}