package fr.yukina.game.graphic.opengl.shader;

import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL44.GL_COMPUTE_SHADER;

public class ShaderUtils
{
	public final static String preprocessShader(String shaderCodeIn)
	{
		// TODO remove comment and unused space
		return shaderCodeIn;
	}

	public static boolean isSpirvSupported()
	{
		return GL.getCapabilities().GL_ARB_gl_spirv;
	}

	public static ByteBuffer loadSpirvSource(String filePath)
	{
		Path path = Paths.get(filePath);
		try
		{
			byte[]     bytes  = Files.readAllBytes(path);
			ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
			buffer.put(bytes);
			buffer.flip();
			return buffer;
		}
		catch (IOException e)
		{
			throw new RuntimeException("Failed to load SPIR-V file: " + filePath, e);
		}
	}

	public static String loadShaderSource(String filePath) throws IOException
	{
		Path   path  = Paths.get(filePath);
		byte[] bytes = Files.readAllBytes(path);
		return new String(bytes, StandardCharsets.UTF_8);
	}

	public final static String type(int typeIn)
	{
		switch (typeIn)
		{
			case GL_VERTEX_SHADER:
				return "vertex";
			case GL_FRAGMENT_SHADER:
				return "fragment";
			case GL_TESS_CONTROL_SHADER:
				return "tess control";
			case GL_TESS_EVALUATION_SHADER:
				return "tess evaluation";
			case GL_GEOMETRY_SHADER:
				return "geometry";
			case GL_COMPUTE_SHADER:
				return "compute";
			default:
				return "unknown";
		}
	}
}
