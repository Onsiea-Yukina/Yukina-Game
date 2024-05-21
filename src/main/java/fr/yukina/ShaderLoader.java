package fr.yukina;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL43.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL43.GL_TESS_EVALUATION_SHADER;

public class ShaderLoader
{

	public static int loadShader(String vertexPath, String fragmentPath, String tessControlPath, String tessEvalPath)
	{
		int vertexShader = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShader, readFile(vertexPath));
		glCompileShader(vertexShader);
		checkCompileErrors(vertexShader, "VERTEX");

		int tessControlShader = glCreateShader(GL_TESS_CONTROL_SHADER);
		glShaderSource(tessControlShader, readFile(tessControlPath));
		glCompileShader(tessControlShader);
		checkCompileErrors(tessControlShader, "TESS_CONTROL");

		int tessEvalShader = glCreateShader(GL_TESS_EVALUATION_SHADER);
		glShaderSource(tessEvalShader, readFile(tessEvalPath));
		glCompileShader(tessEvalShader);
		checkCompileErrors(tessEvalShader, "TESS_EVAL");

		int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShader, readFile(fragmentPath));
		glCompileShader(fragmentShader);
		checkCompileErrors(fragmentShader, "FRAGMENT");

		int shaderProgram = glCreateProgram();
		glAttachShader(shaderProgram, vertexShader);
		glAttachShader(shaderProgram, tessControlShader);
		glAttachShader(shaderProgram, tessEvalShader);
		glAttachShader(shaderProgram, fragmentShader);
		glLinkProgram(shaderProgram);
		checkCompileErrors(shaderProgram, "PROGRAM");

		glDeleteShader(vertexShader);
		glDeleteShader(tessControlShader);
		glDeleteShader(tessEvalShader);
		glDeleteShader(fragmentShader);

		return shaderProgram;
	}

	private static String readFile(String path)
	{
		try
		{
			return Files.lines(Paths.get(path)).collect(Collectors.joining("\n"));
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw new RuntimeException("Failed to read shader file: " + path);
		}
	}

	// Check compile and link status
	private static void checkCompileErrors(int shader, String type)
	{
		int success;
		if (type.equals("PROGRAM"))
		{
			success = glGetProgrami(shader, GL_LINK_STATUS);
			if (success == GL_FALSE)
			{
				String infoLog = glGetProgramInfoLog(shader);
				System.err.println("ERROR::SHADER::PROGRAM::LINKING_FAILED\n" + infoLog);
				throw new RuntimeException("Shader program linking failed");
			}
		}
		else
		{
			success = glGetShaderi(shader, GL_COMPILE_STATUS);
			if (success == GL_FALSE)
			{
				String infoLog = glGetShaderInfoLog(shader);
				System.err.println("ERROR::SHADER::" + type + "::COMPILATION_FAILED\n" + infoLog);
				throw new RuntimeException("Shader compilation failed");
			}
		}
	}
}