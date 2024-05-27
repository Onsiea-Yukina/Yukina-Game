package fr.yukina.game.render.opengl;

import org.lwjgl.opengl.GL15;

public class Ibo extends BufferObject
{
	public Ibo()
	{
		super(GL15.GL_ELEMENT_ARRAY_BUFFER);
	}
}
