package fr.yukina;

import org.lwjgl.opengl.GLDebugMessageCallback;

import static org.lwjgl.opengl.GL43.*;

public class DebugCallback extends GLDebugMessageCallback
{
	@Override
	public void invoke(int source, int type, int id, int severity, int length, long message, long userParam)
	{
		String sourceString;
		switch (source)
		{
			case GL_DEBUG_SOURCE_API:
				sourceString = "API";
				break;
			case GL_DEBUG_SOURCE_WINDOW_SYSTEM:
				sourceString = "Window System";
				break;
			case GL_DEBUG_SOURCE_SHADER_COMPILER:
				sourceString = "Shader Compiler";
				break;
			case GL_DEBUG_SOURCE_THIRD_PARTY:
				sourceString = "Third Party";
				break;
			case GL_DEBUG_SOURCE_APPLICATION:
				sourceString = "Application";
				break;
			case GL_DEBUG_SOURCE_OTHER:
				sourceString = "Other";
				break;
			default:
				sourceString = "Unknown";
				break;
		}

		String typeString;
		switch (type)
		{
			case GL_DEBUG_TYPE_ERROR:
				typeString = "Error";
				break;
			case GL_DEBUG_TYPE_DEPRECATED_BEHAVIOR:
				typeString = "Deprecated Behavior";
				break;
			case GL_DEBUG_TYPE_UNDEFINED_BEHAVIOR:
				typeString = "Undefined Behavior";
				break;
			case GL_DEBUG_TYPE_PORTABILITY:
				typeString = "Portability";
				break;
			case GL_DEBUG_TYPE_PERFORMANCE:
				typeString = "Performance";
				break;
			case GL_DEBUG_TYPE_MARKER:
				typeString = "Marker";
				break;
			case GL_DEBUG_TYPE_PUSH_GROUP:
				typeString = "Push Group";
				break;
			case GL_DEBUG_TYPE_POP_GROUP:
				typeString = "Pop Group";
				break;
			case GL_DEBUG_TYPE_OTHER:
				typeString = "Other";
				break;
			default:
				typeString = "Unknown";
				break;
		}

		String severityString;
		switch (severity)
		{
			case GL_DEBUG_SEVERITY_HIGH:
				severityString = "High";
				break;
			case GL_DEBUG_SEVERITY_MEDIUM:
				severityString = "Medium";
				break;
			case GL_DEBUG_SEVERITY_LOW:
				severityString = "Low";
				break;
			case GL_DEBUG_SEVERITY_NOTIFICATION:
				severityString = "Notification";
				break;
			default:
				severityString = "Unknown";
				break;
		}

		String msg = GLDebugMessageCallback.getMessage(length, message);

		System.err.printf("OpenGL Error [Source: %s] [Type: %s] [ID: %d] [Severity: %s]: %s\n", sourceString,
		                  typeString, id, severityString, msg);
	}
}