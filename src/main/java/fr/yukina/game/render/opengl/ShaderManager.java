package fr.yukina.game.render.opengl;

import lombok.Getter;
import org.lwjgl.opengl.GL20;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Getter
public class ShaderManager
{
	private int vertexShaderId;
	private int fragmentShaderId;
	private int programId;

	public void loadShaders(String vertexShaderFileIn, String fragmentShaderFileIn) throws IOException
	{
		String vertexShaderSource   = new String(Files.readAllBytes(Paths.get(vertexShaderFileIn)));
		String fragmentShaderSource = new String(Files.readAllBytes(Paths.get(fragmentShaderFileIn)));

		this.vertexShaderId   = createShader(vertexShaderSource, GL20.GL_VERTEX_SHADER);
		this.fragmentShaderId = createShader(fragmentShaderSource, GL20.GL_FRAGMENT_SHADER);

		this.programId = GL20.glCreateProgram();
		GL20.glAttachShader(this.programId, this.vertexShaderId);
		GL20.glAttachShader(this.programId, this.fragmentShaderId);
		GL20.glLinkProgram(this.programId);

		if (GL20.glGetProgrami(this.programId, GL20.GL_LINK_STATUS) == 0)
		{
			throw new RuntimeException("Error linking Shader code: " + GL20.glGetProgramInfoLog(this.programId, 1024));
		}

		GL20.glValidateProgram(this.programId);
		if (GL20.glGetProgrami(this.programId, GL20.GL_VALIDATE_STATUS) == 0)
		{
			System.err.println("Warning validating Shader code: " + GL20.glGetProgramInfoLog(this.programId, 1024));
		}
	}

	private int createShader(String shaderCodeIn, int shaderTypeIn)
	{
		int shaderId = GL20.glCreateShader(shaderTypeIn);
		if (shaderId == 0)
		{
			throw new RuntimeException("Error creating shader. Type: " + shaderTypeIn);
		}

		GL20.glShaderSource(shaderId, shaderCodeIn);
		GL20.glCompileShader(shaderId);

		if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == 0)
		{
			throw new RuntimeException("Error compiling Shader code: " + GL20.glGetShaderInfoLog(shaderId, 1024));
		}

		return shaderId;
	}

	public void attach()
	{
		GL20.glUseProgram(this.programId);
	}

	public void detach()
	{
		GL20.glUseProgram(0);
	}

	public void cleanup()
	{
		GL20.glUseProgram(0);
		if (this.programId != 0)
		{
			GL20.glDetachShader(this.programId, this.vertexShaderId);
			GL20.glDetachShader(this.programId, this.fragmentShaderId);
		}
		if (this.vertexShaderId != 0)
		{
			GL20.glDeleteShader(this.vertexShaderId);
		}
		if (this.fragmentShaderId != 0)
		{
			GL20.glDeleteShader(this.fragmentShaderId);
		}
		if (this.programId != 0)
		{
			GL20.glDeleteProgram(this.programId);
		}
	}
}