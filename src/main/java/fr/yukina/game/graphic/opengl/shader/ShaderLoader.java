package fr.yukina.game.graphic.opengl.shader;

import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.ARBGLSPIRV.GL_SHADER_BINARY_FORMAT_SPIR_V_ARB;
import static org.lwjgl.opengl.ARBGLSPIRV.glSpecializeShaderARB;
import static org.lwjgl.opengl.GL41C.*;

public class ShaderLoader
{
	private final Map<Integer, String> shaderFilePaths;
	private final boolean              verbose;
	private final ShaderProgram        shaderProgram;

	public ShaderLoader(Map<Integer, String> shaderFilePaths, boolean verbose, ShaderProgram shaderProgram)
	{
		this.shaderFilePaths = shaderFilePaths;
		this.verbose         = verbose;
		this.shaderProgram   = shaderProgram;

		ShaderManager.fileWatcher().registerShaderProgram(shaderProgram);
	}

	public boolean loadProgramBinary()
	{
		try
		{
			ShaderProgramBinary binary = ShaderCache.loadProgramBinaryFromCache(shaderFilePaths);
			if (binary != null)
			{
				glProgramBinary(shaderProgram.programId(), binary.format(), binary.binary());
				if (glGetProgrami(shaderProgram.programId(), GL_LINK_STATUS) == GL_TRUE)
				{
					return true;
				}
				else
				{
					if (verbose)
					{
						System.err.println("Error linking shader program from binary cache: " + glGetProgramInfoLog(
								shaderProgram.programId()));
					}
				}
			}
		}
		catch (IOException | NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public boolean loadSpirvShaders()
	{
		boolean spirvLoaded = false;
		for (Map.Entry<Integer, String> entry : shaderFilePaths.entrySet())
		{
			String shaderFilePath = entry.getValue();
			String spirvFilePath  = shaderFilePath.endsWith(".spv") ? shaderFilePath : shaderFilePath + ".spv";
			Path   spirvPath      = Paths.get(spirvFilePath);
			Path   shaderPath     = Paths.get(shaderFilePath);

			if (Files.exists(spirvPath))
			{
				shaderFilePath = spirvFilePath;  // Use SPIR-V file if it exists
			}
			else if (!Files.exists(shaderPath))
			{
				throw new RuntimeException("Shader file does not exist: " + shaderFilePath);
			}

			if (shaderFilePath.endsWith(".spv") || shaderFilePath.endsWith(".spirv"))
			{
				int shaderId = glCreateShader(entry.getKey());
				if (shaderId == 0)
				{
					throw new RuntimeException("Error creating shader");
				}

				ByteBuffer spirvCode = ShaderUtils.loadSpirvSource(shaderFilePath);
				ByteBuffer entryPoint = ByteBuffer.allocateDirect("main".length() + 1)
				                                  .put("main".getBytes(StandardCharsets.UTF_8)).put((byte) 0);
				entryPoint.flip();
				int[] emptyConstants = new int[0];

				glShaderBinary(new int[] { shaderId }, GL_SHADER_BINARY_FORMAT_SPIR_V_ARB, spirvCode);
				glSpecializeShaderARB(shaderId, entryPoint, emptyConstants, emptyConstants);

				if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE)
				{
					throw new RuntimeException("Error compiling SPIR-V shader code: " + glGetShaderInfoLog(shaderId));
				}

				glAttachShader(shaderProgram.programId(), shaderId);
				spirvLoaded = true;
			}
		}
		if (spirvLoaded)
		{
			shaderProgram.link();
			saveProgramBinary(false);
		}
		return spirvLoaded;
	}

	public void compileAndLinkShaders()
	{
		try
		{
			Map<Integer, String> shaderCodes = new HashMap<>();
			for (Map.Entry<Integer, String> entry : shaderFilePaths.entrySet())
			{
				String shaderCode = ShaderUtils.loadShaderSource(entry.getValue());
				shaderCode = ShaderUtils.preprocessShader(shaderCode);
				ShaderValidator.validate(entry.getKey(), shaderCode);
				shaderCodes.put(entry.getKey(), shaderCode);
			}

			for (Map.Entry<Integer, String> entry : shaderCodes.entrySet())
			{
				shaderProgram.createShader(entry.getValue(), entry.getKey());
			}
			shaderProgram.link();
			saveProgramBinary(false);
			if (verbose)
			{
				System.out.println("Shader program compiled and cached.");
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void saveProgramBinary(boolean saveShaderCode)
	{
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer lengthBuffer = stack.mallocInt(1);
			glGetProgramiv(shaderProgram.programId(), GL_PROGRAM_BINARY_LENGTH, lengthBuffer);
			ByteBuffer binary = stack.malloc(lengthBuffer.get(0));
			IntBuffer  format = stack.mallocInt(1);
			glGetProgramBinary(shaderProgram.programId(), lengthBuffer, format, binary);

			ShaderCache.saveShaderToCache(shaderFilePaths, binary, format.get(0), saveShaderCode);
		}
		catch (IOException | NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
	}
}
