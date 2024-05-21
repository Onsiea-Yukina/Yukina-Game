package fr.yukina;

import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glGetError;

public class Utils
{
	public static void checkGLError(String location)
	{
		int error;
		while ((error = glGetError()) != GL_NO_ERROR)
		{
			System.err.println("OpenGL Error at " + location + ": " + error);
		}
	}
}