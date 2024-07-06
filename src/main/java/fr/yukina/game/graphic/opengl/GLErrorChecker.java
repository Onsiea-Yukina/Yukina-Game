package fr.yukina.game.graphic.opengl;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class GLErrorChecker
{
	public static void checkForGLErrors(String context)
	{
		int error;
		while ((error = GL11.glGetError()) != GL11.GL_NO_ERROR)
		{
			String errorString;
			switch (error)
			{
				case GL11.GL_INVALID_ENUM:
					errorString = "GL_INVALID_ENUM";
					break;
				case GL11.GL_INVALID_VALUE:
					errorString = "GL_INVALID_VALUE";
					break;
				case GL11.GL_INVALID_OPERATION:
					errorString = "GL_INVALID_OPERATION";
					break;
				case GL11.GL_STACK_OVERFLOW:
					errorString = "GL_STACK_OVERFLOW";
					break;
				case GL11.GL_STACK_UNDERFLOW:
					errorString = "GL_STACK_UNDERFLOW";
					break;
				case GL11.GL_OUT_OF_MEMORY:
					errorString = "GL_OUT_OF_MEMORY";
					break;
				case GL30.GL_INVALID_FRAMEBUFFER_OPERATION:
					errorString = "GL_INVALID_FRAMEBUFFER_OPERATION";
					break;
				default:
					errorString = "Unknown error";
			}
			System.err.println("OpenGL Error [" + context + "]: " + errorString);
		}
	}
}