package fr.yukina.game.render.opengl;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL32;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class OpenGLValidator
{

	public static void validateGraphicObject(String prefixIn, ShaderManager shaderManager, GraphicObject graphicObject,
	                                         boolean showDataIn)
	{
		// Check VAO binding
		if (graphicObject.vertexArrayObject() != null)
		{
			graphicObject.vertexArrayObject().attach();
		}
		else
		{
			System.out.println(prefixIn + "VAO is not set.");
			return;
		}

		// Validate shader program
		shaderManager.attach();

		// Get the number of attributes in the shader program
		int programID     = shaderManager.programId();
		int numAttributes = GL32.glGetProgrami(programID, GL32.GL_ACTIVE_ATTRIBUTES);
		System.out.println(prefixIn + "Number of active attributes: " + numAttributes);

		// Validate each attribute
		for (int i = 0; i < numAttributes; i++)
		{
			IntBuffer  lengthBuffer = BufferUtils.createIntBuffer(1);
			IntBuffer  sizeBuffer   = BufferUtils.createIntBuffer(1);
			IntBuffer  typeBuffer   = BufferUtils.createIntBuffer(1);
			ByteBuffer nameBuffer   = BufferUtils.createByteBuffer(256);

			GL32.glGetActiveAttrib(programID, i, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
			nameBuffer.limit(lengthBuffer.get(0));
			String name     = getStringUTF8(nameBuffer);
			int    location = GL32.glGetAttribLocation(programID, name);

			if (location < 0)
			{
				System.out.println(prefixIn + "Attribute " + name + " is not found in the shader program.");
				continue;
			}
			System.out.println(prefixIn + "Attribute " + name + " is at location: " + location);

			// Check if the attribute is enabled
			IntBuffer enabledBuffer = BufferUtils.createIntBuffer(1);
			GL32.glGetVertexAttribiv(location, GL32.GL_VERTEX_ATTRIB_ARRAY_ENABLED, enabledBuffer);
			boolean enabled = enabledBuffer.get(0) == GL32.GL_TRUE;

			if (!enabled)
			{
				System.out.println(prefixIn + "Attribute " + name + " at location " + location + " is not enabled.");
			}
			else
			{
				System.out.println(prefixIn + "Attribute " + name + " at location " + location + " is enabled.");
			}

			// Check the attribute pointer settings
			GL32.glGetVertexAttribiv(location, GL32.GL_VERTEX_ATTRIB_ARRAY_SIZE, sizeBuffer);
			GL32.glGetVertexAttribiv(location, GL32.GL_VERTEX_ATTRIB_ARRAY_TYPE, typeBuffer);
			int size = sizeBuffer.get(0);
			int type = typeBuffer.get(0);
			System.out.println(prefixIn + "Attribute " + name + " has size: " + size + " and type: " + type);
		}

		// Validate VBOs
		IntBuffer buffers = BufferUtils.createIntBuffer(1);
		GL32.glGetIntegerv(GL32.GL_ARRAY_BUFFER_BINDING, buffers);
		if (buffers.get(0) == 0)
		{
			System.out.println(prefixIn + "No VBO is currently bound to GL_ARRAY_BUFFER.");
		}
		else
		{
			System.out.println(prefixIn + "VBO bound to GL_ARRAY_BUFFER: " + buffers.get(0));
		}

		GL32.glGetIntegerv(GL32.GL_ELEMENT_ARRAY_BUFFER_BINDING, buffers);
		if (buffers.get(0) == 0)
		{
			System.out.println(prefixIn + "No VBO is currently bound to GL_ELEMENT_ARRAY_BUFFER.");
		}
		else
		{
			System.out.println(prefixIn + "VBO bound to GL_ELEMENT_ARRAY_BUFFER: " + buffers.get(0));
		}

		// Validate index buffer
		if (graphicObject.indices() != null)
		{
			if (showDataIn)
			{
				graphicObject.indices().attach();
				int       indexCount    = graphicObject.indices().count();
				IntBuffer indicesBuffer = BufferUtils.createIntBuffer(indexCount);
				GL32.glGetBufferSubData(GL32.GL_ELEMENT_ARRAY_BUFFER, 0, indicesBuffer);
				System.out.println(prefixIn + "Index buffer is valid and contains " + indexCount + " indices.");

				// Additional index buffer validation
				validateIndexBuffer(prefixIn, indicesBuffer, indexCount, graphicObject.bufferObjects().size());
			}
		}
		else
		{
			System.out.println(prefixIn + "Index buffer is not set.");
		}

		// Validate vertex data
		for (var bufferObject : graphicObject.bufferObjects())
		{
			if (bufferObject != null)
			{
				if (showDataIn)
				{
					bufferObject.attach();
					int         vertexCount  = bufferObject.count();
					FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(vertexCount);
					GL32.glGetBufferSubData(GL32.GL_ARRAY_BUFFER, 0, vertexBuffer);
					System.out.println(prefixIn + "Vertex buffer is valid and contains " + vertexCount + " vertices.");

					// Additional vertex buffer validation
					validateVertexBuffer(prefixIn, vertexBuffer, vertexCount);
				}
			}
			else
			{
				System.out.println(prefixIn + "Vertex buffer is not set.");
			}
		}

		// Check OpenGL errors
		int error;
		while ((error = GL32.glGetError()) != GL32.GL_NO_ERROR)
		{
			System.out.println(prefixIn + "OpenGL Error: " + error);
		}

		// Unbind the VAO
		graphicObject.detach();
	}

	private static String getStringUTF8(ByteBuffer buffer)
	{
		StringBuilder sb = new StringBuilder();
		while (buffer.hasRemaining())
		{
			char c = (char) buffer.get();
			if (c == 0)
			{
				break; // null terminator
			}
			sb.append(c);
		}
		return sb.toString();
	}

	private static void validateIndexBuffer(String prefixIn, IntBuffer indicesBuffer, int indexCount, int vertexCount)
	{
		if (indexCount == 0)
		{
			System.out.println(prefixIn + "Index buffer is empty.");
		}
		else
		{
			for (int i = 0; i < indexCount; i++)
			{
				int index = indicesBuffer.get(i);
				if (index < 0 || index >= vertexCount)
				{
					System.out.println(prefixIn + "Index " + index + " at position " + i + " is out of range.");
				}
				if (i > 0 && indicesBuffer.get(i) == indicesBuffer.get(i - 1))
				{
					System.out.println(prefixIn + "Index " + index + " is repeated at position " + i + ".");
				}
			}
		}
	}

	private static void validateVertexBuffer(String prefixIn, FloatBuffer vertexBuffer, int vertexCount)
	{
		if (vertexCount == 0)
		{
			System.out.println(prefixIn + "Vertex buffer is empty.");
		}
		else
		{
			for (int i = 0; i < vertexCount; i++)
			{
				float vertex = vertexBuffer.get(i);
				if (i > 0 && vertexBuffer.get(i) == vertexBuffer.get(i - 1))
				{
					System.out.println(prefixIn + "Vertex value " + vertex + " is repeated at position " + i + ".");
				}
			}
		}
	}
}