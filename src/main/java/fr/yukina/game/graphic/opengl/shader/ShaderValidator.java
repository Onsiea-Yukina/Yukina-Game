package fr.yukina.game.graphic.opengl.shader;

import org.lwjgl.opengl.GL20;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderValidator
{

	private static final Pattern OUTPUT_VAR_PATTERN = Pattern.compile("out\\s+\\w+\\s+(\\w+);");
	private static final Pattern COMMENT_PATTERN    = Pattern.compile("(//.*|/\\*((.|\\n)(?!=*/))+\\*/)");

	public static void validate(int typeIn, String shaderCode)
	{
		checkSyntax(shaderCode);
		checkCommonProblems(shaderCode);
		checkOutputVariableNames(typeIn, shaderCode);
	}

	private static void checkSyntax(String shaderCode)
	{
		// Syntax checking can be complex and might require parsing the GLSL code
		// For simplicity, we'll just ensure there are no unbalanced brackets
		int openBrackets = 0;
		for (char c : shaderCode.toCharArray())
		{
			if (c == '{')
			{
				openBrackets++;
			}
			if (c == '}')
			{
				openBrackets--;
			}
		}
		if (openBrackets != 0)
		{
			throw new RuntimeException("Syntax error: Unbalanced brackets in shader code.");
		}
	}

	private static void checkCommonProblems(String shaderCode)
	{
		// Example check for deprecated GLSL functions or common issues
		if (shaderCode.contains("gl_FragColor"))
		{
			throw new RuntimeException("Common problem: 'gl_FragColor' is deprecated. Use 'out' variables instead.");
		}
	}

	private static void checkOutputVariableNames(int typeIn, String shaderCode)
	{
		if (typeIn == GL20.GL_FRAGMENT_SHADER)
		{
			return;
		}

		Matcher matcher = OUTPUT_VAR_PATTERN.matcher(shaderCode);
		while (matcher.find())
		{
			String varName = matcher.group(1);
			if (!varName.startsWith("pass"))
			{
				throw new RuntimeException("Output variable '" + varName
				                           + "' does not follow naming convention. It should start with 'pass'.");
			}
		}
	}

	public static String preprocessShader(String shaderCode)
	{
		// Remove comments
		return COMMENT_PATTERN.matcher(shaderCode).replaceAll("");
	}
}
