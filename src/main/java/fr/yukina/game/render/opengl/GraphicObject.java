package fr.yukina.game.render.opengl;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GraphicObject
{
	private final   VertexArrayObject  vertexArrayObject;
	private final   List<BufferObject> bufferObjects;
	private @Setter Ibo                indices;
	private @Setter int                count;

	public GraphicObject()
	{
		this.vertexArrayObject = new VertexArrayObject();
		this.bufferObjects     = new ArrayList<>();
	}

	public void attach()
	{
		this.vertexArrayObject.attach();
	}

	public BufferObject bufferObject(int targetIn, int sizeIn, int strideIn, int offsetIn)
	{
		var bufferObject = new BufferObject(targetIn);
		bufferObject.attach();
		this.vertexArrayObject.attribute(this.bufferObjects.size(), sizeIn, strideIn, offsetIn);
		this.bufferObjects.add(bufferObject);

		return bufferObject;
	}

	public Ibo indexBufferObject()
	{
		this.indices = new Ibo();
		this.indices.attach();

		return this.indices;
	}

	public void render()
	{
		this.attach();
		if (this.indices != null)
		{
			GL11.glDrawElements(GL11.GL_TRIANGLES, this.indices.count(), GL11.GL_UNSIGNED_INT, 0L);
		}
		else
		{
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, this.count);
		}
		this.detach();
	}

	public void detach()
	{
		this.vertexArrayObject.detach();
	}

	public void cleanup()
	{
		this.vertexArrayObject.cleanup();
		for (var bufferObject : this.bufferObjects)
		{
			bufferObject.cleanup();
		}
		if (this.indices != null)
		{
			this.indices.cleanup();
		}
	}
}