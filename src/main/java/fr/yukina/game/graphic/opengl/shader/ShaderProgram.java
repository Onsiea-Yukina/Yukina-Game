package fr.yukina.game.graphic.opengl.shader;

import lombok.Getter;
import org.joml.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL41C.*;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public class ShaderProgram
{
	private static final ThreadLocal<FloatBuffer> MATRIX_BUFFER   = ThreadLocal.withInitial(
			() -> BufferUtils.createFloatBuffer(16));
	private static final Pattern                  COMMENT_PATTERN = Pattern.compile(
			"(//.*|/\\*((.|\\n)(?!=*/))" + "+\\*/)");

	private final Map<Integer, String> shaderFilePaths;
	private final Map<String, Integer> uniforms;
	private final Map<String, Integer> attributes;
	private final boolean              verbose;
	private       ShaderLoader         shaderLoader;

	private @Getter int programId;
	private         int attributeCounter;

	public ShaderProgram(Map<Integer, String> shaderFilePaths, boolean verbose)
	{
		this.shaderFilePaths = shaderFilePaths;
		this.uniforms        = new HashMap<>();
		this.attributes      = new HashMap<>();
		this.verbose         = verbose;
		this.shaderLoader    = new ShaderLoader(shaderFilePaths, verbose, this);

		this.programId = glCreateProgram();
		if (this.programId == 0)
		{
			throw new RuntimeException("Cannot create shader program ID");
		}

		if (!shaderLoader.loadProgramBinary())
		{
			this.forceLoad();
		}
		else
		{
			if (verbose)
			{
				System.out.println("Loaded shader program from binary cache.");
			}
		}

		reflectAttributes();
		reflectUniforms();
	}

	public void reloadShaders()
	{
		if (verbose)
		{
			System.out.println("Shader files changed, reloading...");
		}

		this.cleanup();
		var shaderProgramBuilder = new ShaderProgramBuilder(this.verbose);
		for (var entry : this.shaderFilePaths.entrySet())
		{
			shaderProgramBuilder.add(entry.getKey(), entry.getValue());
		}
		var shaderProgram = shaderProgramBuilder.build();

		this.programId = shaderProgram.programId;
		this.attributes.clear();
		for (var entry : shaderProgram.attributes.entrySet())
		{
			this.attributes.put(entry.getKey(), entry.getValue());
		}
		this.uniforms.clear();
		for (var entry : shaderProgram.uniforms.entrySet())
		{
			this.uniforms.put(entry.getKey(), entry.getValue());
		}
		this.attach();
	}

	private void forceLoad()
	{
		if (ShaderUtils.isSpirvSupported() && this.shaderLoader.loadSpirvShaders())
		{
			if (this.verbose)
			{
				System.out.println("Loaded shader program from SPIR-V files.");
			}
		}
		else
		{
			this.shaderLoader.compileAndLinkShaders();
		}
	}

	public void createShader(String shaderCodeIn, int shaderTypeIn)
	{
		int shaderId = glCreateShader(shaderTypeIn);
		if (shaderId == 0)
		{
			throw new RuntimeException("Error creating shader");
		}

		glShaderSource(shaderId, shaderCodeIn);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE)
		{
			throw new RuntimeException("Error compiling shader code: " + glGetShaderInfoLog(shaderId));
		}

		glAttachShader(this.programId, shaderId);
	}

	public void link()
	{
		glLinkProgram(this.programId);
		if (glGetProgrami(this.programId, GL_LINK_STATUS) == GL_FALSE)
		{
			throw new RuntimeException("Error linking shader program: " + glGetProgramInfoLog(this.programId));
		}

		IntBuffer shaderCount = BufferUtils.createIntBuffer(1);
		glGetProgramiv(this.programId, GL_ATTACHED_SHADERS, shaderCount);

		IntBuffer shaders = BufferUtils.createIntBuffer(shaderCount.get(0));
		glGetAttachedShaders(this.programId, shaderCount, shaders);

		for (int i = 0; i < shaderCount.get(0); i++)
		{
			int shaderId = shaders.get(i);
			glDetachShader(this.programId, shaderId);
		}

		glValidateProgram(this.programId);
		if (glGetProgrami(this.programId, GL_VALIDATE_STATUS) == GL_FALSE)
		{
			if (verbose)
			{
				System.err.println("Warning validating shader program: " + glGetProgramInfoLog(this.programId));
			}
		}
	}

	public void logStatus()
	{
		System.out.println("Shader Program ID: " + this.programId);

		// Check link status
		int linkStatus = glGetProgrami(this.programId, GL_LINK_STATUS);
		if (linkStatus == GL_TRUE)
		{
			System.out.println("Link Status: Success");
		}
		else
		{
			System.out.println("Link Status: Failed");
			System.out.println("Link Log: " + glGetProgramInfoLog(this.programId));
		}

		// Check validation status
		int validateStatus = glGetProgrami(this.programId, GL_VALIDATE_STATUS);
		if (validateStatus == GL_TRUE)
		{
			System.out.println("Validation Status: Success");
		}
		else
		{
			System.out.println("Validation Status: Failed");
			System.out.println("Validation Log: " + glGetProgramInfoLog(this.programId));
		}

		// Log attached shaders
		IntBuffer shaderCount = BufferUtils.createIntBuffer(1);
		glGetProgramiv(this.programId, GL_ATTACHED_SHADERS, shaderCount);
		IntBuffer shaders = BufferUtils.createIntBuffer(shaderCount.get(0));
		glGetAttachedShaders(this.programId, shaderCount, shaders);

		System.out.println("Attached Shaders: " + shaderCount.get(0));
		for (int i = 0; i < shaderCount.get(0); i++)
		{
			int    shaderId       = shaders.get(i);
			int    shaderType     = glGetShaderi(shaderId, GL_SHADER_TYPE);
			String shaderTypeName = ShaderUtils.type(shaderType);
			System.out.println("Shader ID: " + shaderId + ", Type: " + shaderTypeName);
			System.out.println("Shader Log: " + glGetShaderInfoLog(shaderId));
		}

		// Log active uniforms
		int numUniforms = glGetProgrami(this.programId, GL_ACTIVE_UNIFORMS);
		System.out.println("Active Uniforms: " + numUniforms);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer  sizeBuffer   = stack.mallocInt(1);
			IntBuffer  typeBuffer   = stack.mallocInt(1);
			ByteBuffer nameBuffer   = stack.malloc(256);
			IntBuffer  lengthBuffer = stack.mallocInt(1);

			for (int i = 0; i < numUniforms; i++)
			{
				glGetActiveUniform(this.programId, i, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
				String uniformName = getStringFromBuffer(nameBuffer, lengthBuffer.get(0));
				int    location    = glGetUniformLocation(this.programId, uniformName);
				System.out.println(
						"Uniform Name: " + uniformName + ", Location: " + location + ", Size: " + sizeBuffer.get(0)
						+ ", Type: " + ShaderUtils.type(typeBuffer.get(0)));
			}
		}

		// Log active attributes
		int numAttributes = glGetProgrami(this.programId, GL_ACTIVE_ATTRIBUTES);
		System.out.println("Active Attributes: " + numAttributes);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer  sizeBuffer   = stack.mallocInt(1);
			IntBuffer  typeBuffer   = stack.mallocInt(1);
			ByteBuffer nameBuffer   = stack.malloc(256);
			IntBuffer  lengthBuffer = stack.mallocInt(1);

			for (int i = 0; i < numAttributes; i++)
			{
				glGetActiveAttrib(this.programId, i, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
				String attributeName = getStringFromBuffer(nameBuffer, lengthBuffer.get(0));
				int    location      = glGetAttribLocation(this.programId, attributeName);
				System.out.println(
						"Attribute Name: " + attributeName + ", Location: " + location + ", Size: " + sizeBuffer.get(0)
						+ ", Type: " + ShaderUtils.type(typeBuffer.get(0)));
			}
		}
	}

	private void reflectAttributes()
	{
		int numAttributes = glGetProgrami(this.programId, GL_ACTIVE_ATTRIBUTES);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer  sizeBuffer   = stack.mallocInt(1);
			IntBuffer  typeBuffer   = stack.mallocInt(1);
			ByteBuffer nameBuffer   = stack.malloc(256);
			IntBuffer  lengthBuffer = stack.mallocInt(1);

			for (int i = 0; i < numAttributes; i++)
			{
				glGetActiveAttrib(this.programId, i, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
				String attributeName = getStringFromBuffer(nameBuffer, lengthBuffer.get(0));
				int    location      = glGetAttribLocation(this.programId, attributeName);
				attributes.put(attributeName, location);
			}
		}
	}

	private void reflectUniforms()
	{
		int numUniforms = glGetProgrami(this.programId, GL_ACTIVE_UNIFORMS);
		try (MemoryStack stack = MemoryStack.stackPush())
		{
			IntBuffer  sizeBuffer   = stack.mallocInt(1);
			IntBuffer  typeBuffer   = stack.mallocInt(1);
			ByteBuffer nameBuffer   = stack.malloc(256);
			IntBuffer  lengthBuffer = stack.mallocInt(1);

			for (int i = 0; i < numUniforms; i++)
			{
				glGetActiveUniform(this.programId, i, lengthBuffer, sizeBuffer, typeBuffer, nameBuffer);
				String uniformName = getStringFromBuffer(nameBuffer, lengthBuffer.get(0));
				int    location    = glGetUniformLocation(this.programId, uniformName);
				uniforms.put(uniformName, location);
			}
		}
	}

	private String getStringFromBuffer(ByteBuffer buffer, int length)
	{
		byte[] bytes = new byte[length];
		buffer.get(bytes, 0, length);
		buffer.clear();
		return new String(bytes).trim();
	}

	public int uniform(String nameIn)
	{
		return uniforms.computeIfAbsent(nameIn, k ->
		{
			int uniformLocation = glGetUniformLocation(this.programId, k);
			if (uniformLocation < 0)
			{
				System.err.println("Error getting uniform location for " + k);
				return -1;
			}
			return uniformLocation;
		});
	}

	public void uniform(String nameIn, int valueIn)
	{
		glUniform1i(this.uniform(nameIn), valueIn);
	}

	public void uniform(int locationIn, int valueIn)
	{
		glUniform1i(locationIn, valueIn);
	}

	public void uniform(String nameIn, int[] valueIn)
	{
		glUniform1iv(this.uniform(nameIn), valueIn);
	}

	public void uniform(int locationIn, int[] valueIn)
	{
		glUniform1iv(locationIn, valueIn);
	}

	public void uniform(String nameIn, Vector2i valueIn)
	{
		glUniform2i(this.uniform(nameIn), valueIn.x, valueIn.y);
	}

	public void uniform(int locationIn, Vector2i valueIn)
	{
		glUniform2i(locationIn, valueIn.x, valueIn.y);
	}

	public void uniform(String nameIn, Vector3i valueIn)
	{
		glUniform3i(this.uniform(nameIn), valueIn.x, valueIn.y, valueIn.z);
	}

	public void uniform(int locationIn, Vector3i valueIn)
	{
		glUniform3i(locationIn, valueIn.x, valueIn.y, valueIn.z);
	}

	public void uniform(String nameIn, Vector4i valueIn)
	{
		glUniform4i(this.uniform(nameIn), valueIn.x, valueIn.y, valueIn.z, valueIn.w);
	}

	public void uniform(int locationIn, Vector4i valueIn)
	{
		glUniform4i(locationIn, valueIn.x, valueIn.y, valueIn.z, valueIn.w);
	}

	public void uniform(String nameIn, float valueIn)
	{
		glUniform1f(this.uniform(nameIn), valueIn);
	}

	public void uniform(int locationIn, float valueIn)
	{
		glUniform1f(locationIn, valueIn);
	}

	public void uniform(String nameIn, float[] valueIn)
	{
		glUniform1fv(this.uniform(nameIn), valueIn);
	}

	public void uniform(int locationIn, float[] valueIn)
	{
		glUniform1fv(locationIn, valueIn);
	}

	public void uniform(String nameIn, Vector2f valueIn)
	{
		glUniform2f(this.uniform(nameIn), valueIn.x, valueIn.y);
	}

	public void uniform(int locationIn, Vector2f valueIn)
	{
		glUniform2f(locationIn, valueIn.x, valueIn.y);
	}

	public void uniform(String nameIn, Vector3f valueIn)
	{
		glUniform3f(this.uniform(nameIn), valueIn.x, valueIn.y, valueIn.z);
	}

	public void uniform(int locationIn, Vector3f valueIn)
	{
		glUniform3f(locationIn, valueIn.x, valueIn.y, valueIn.z);
	}

	public void uniform(String nameIn, Vector4f valueIn)
	{
		glUniform4f(this.uniform(nameIn), valueIn.x, valueIn.y, valueIn.z, valueIn.w);
	}

	public void uniform(int locationIn, Vector4f valueIn)
	{
		glUniform4f(locationIn, valueIn.x, valueIn.y, valueIn.z, valueIn.w);
	}

	public void uniform(String nameIn, Matrix3f matrixIn)
	{
		FloatBuffer buffer = MATRIX_BUFFER.get();
		matrixIn.get(buffer);
		glUniformMatrix3fv(this.uniform(nameIn), false, buffer);
	}

	public void uniform(int locationIn, Matrix3f matrixIn)
	{
		FloatBuffer buffer = MATRIX_BUFFER.get();
		matrixIn.get(buffer);
		glUniformMatrix3fv(locationIn, false, buffer);
	}

	public void uniform(String nameIn, Matrix4f matrixIn)
	{
		FloatBuffer buffer = MATRIX_BUFFER.get();
		matrixIn.get(buffer);
		glUniformMatrix4fv(this.uniform(nameIn), false, buffer);
	}

	public void uniform(int locationIn, Matrix4f matrixIn)
	{
		FloatBuffer buffer = MATRIX_BUFFER.get();
		matrixIn.get(buffer);
		glUniformMatrix4fv(locationIn, false, buffer);
	}

	public void attribute(int attributeIn, String variableNameIn)
	{
		glBindAttribLocation(this.programId, attributeIn, variableNameIn);
	}

	public void attribute(String variableNameIn)
	{
		glBindAttribLocation(this.programId, attributes.getOrDefault(variableNameIn, attributeCounter++),
		                     variableNameIn);
	}

	public void attach()
	{
		glUseProgram(this.programId);
	}

	public void detach()
	{
		glUseProgram(0);
	}

	public void cleanup()
	{
		detach();
		this.uniforms.clear();
		this.attributes.clear();
		this.attributeCounter = 0;
		if (this.programId != 0)
		{
			glDeleteProgram(this.programId);
		}
	}

	public boolean isShaderFile(Path filePath)
	{
		for (var shaderFilePath : shaderFilePaths.values())
		{
			if (shaderFilePath.equals(filePath.toString().replace("\\", "/")))
			{
				return true;
			}
		}

		return false;
	}

	private String createMessage(String messageIn)
	{
		return createMessage("ERROR", messageIn);
	}

	private String createMessage(String typeIn, String messageIn)
	{
		return "[" + typeIn + "] ShaderProgram: " + messageIn.replace("{program-id}", String.valueOf(this.programId));
	}

	private String createMessage(String messageIn, int shaderIdIn, int shaderTypeIn)
	{
		return createMessage("ERROR", shaderIdIn, shaderTypeIn, messageIn);
	}

	private String createMessage(String typeIn, int shaderIdIn, int shaderTypeIn, String messageIn)
	{
		String message = createMessage(typeIn, messageIn).replace("{shader-id}", String.valueOf(shaderIdIn))
		                                                 .replace("{type}", ShaderUtils.type(shaderTypeIn))
		                                                 .replace("{type-id}", String.valueOf(shaderTypeIn));

		int infoLength = glGetShaderi(shaderIdIn, GL_INFO_LOG_LENGTH);
		message = message.replace("{info}", glGetShaderInfoLog(shaderIdIn, infoLength))
		                 .replace("{info-length}", String.valueOf(infoLength));

		return message;
	}

	private String createProgramMessage(String messageIn)
	{
		String message    = createMessage(messageIn);
		int    infoLength = glGetProgrami(this.programId, GL_INFO_LOG_LENGTH);
		return message.replace("{info}", glGetProgramInfoLog(this.programId, infoLength))
		              .replace("{info-length}", String.valueOf(infoLength));
	}

	public static class ShaderProgramBuilder
	{
		private final boolean              verbose;
		private final Map<Integer, String> shadersPaths;

		public ShaderProgramBuilder(boolean verboseIn)
		{
			this.verbose      = verboseIn;
			this.shadersPaths = new HashMap<>();
		}

		public ShaderProgramBuilder vertex(String shaderPathIn)
		{
			this.shadersPaths.put(GL_VERTEX_SHADER, shaderPathIn);
			return this;
		}

		public ShaderProgramBuilder fragment(String shaderPathIn)
		{
			this.shadersPaths.put(GL_FRAGMENT_SHADER, shaderPathIn);
			return this;
		}

		public ShaderProgramBuilder geometry(String shaderPathIn)
		{
			this.shadersPaths.put(GL_GEOMETRY_SHADER, shaderPathIn);
			return this;
		}

		public ShaderProgramBuilder tessellationControl(String shaderPathIn)
		{
			this.shadersPaths.put(GL_TESS_CONTROL_SHADER, shaderPathIn);
			return this;
		}

		public ShaderProgramBuilder tessellationEvaluation(String shaderPathIn)
		{
			this.shadersPaths.put(GL_TESS_EVALUATION_SHADER, shaderPathIn);
			return this;
		}

		public ShaderProgramBuilder compute(String shaderPathIn)
		{
			this.shadersPaths.put(GL_COMPUTE_SHADER, shaderPathIn);
			return this;
		}

		public ShaderProgramBuilder add(int shaderTypeIn, String shaderPathIn)
		{
			this.shadersPaths.put(shaderTypeIn, shaderPathIn);
			return this;
		}

		public ShaderProgram build()
		{
			return new ShaderProgram(this.shadersPaths, this.verbose);
		}
	}
}
