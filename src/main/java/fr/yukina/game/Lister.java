package fr.yukina.game;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

public class Lister
{
	private static final StringBuilder STRING_BUILDER = new StringBuilder();

	public static void main(String[] args)
	{
		var blackList = new String[] { ".git", ".idea"
		};
		/*printSeparator(40);
		STRING_BUILDER.append("Conventions: ").append("\r\n");
		STRING_BUILDER.append("- Use \"In\" suffix for parameters").append("\r\n");
		STRING_BUILDER.append("- Doesn't use \"get\", \"set\" or \"is\" prefix").append("\r\n");
		STRING_BUILDER.append("- Use \"this\" for class variables and methods use").append("\r\n");
		STRING_BUILDER.append("- Open braces always after new line").append("\r\n");
		STRING_BUILDER.append("- Use early return instead nested blocks").append("\r\n");
		STRING_BUILDER.append("- Use Lombok for getter, setter, simple constructor, toString, equals and hashCode ...")
		              .append("\r\n");
		printSeparator(40);
		STRING_BUILDER.append("Project :").append("\r\n");
		STRING_BUILDER.append("- Name : YukinaGame").append("\r\n");
		STRING_BUILDER.append("- Organization : Yukina").append("\r\n");
		STRING_BUILDER.append("- Resources : \"resources/\"").append("\r\n");
		STRING_BUILDER.append("- Main package : fr.yukina.game").append("\r\n");
		STRING_BUILDER.append("- Language : Java").append("\r\n");
		STRING_BUILDER.append("- Librairies :").append("\r\n");
		STRING_BUILDER.append("  - LWJGL").append("\r\n");
		STRING_BUILDER.append("  - Lombok").append("\r\n");
		STRING_BUILDER.append("  - GSON").append("\r\n");
		printSeparator(40);
		STRING_BUILDER.append("Project current structure :").append("\r\n");
		listFiles(new File("."), blackList, 0, "*");
		printSeparator(40);
		STRING_BUILDER.append("Project current shaders : ").append("\r\n");
		listCodes(new File("resources/shaders"), blackList, 0, ".glsl", ".frag", ".vert");
		printSeparator(40);
		STRING_BUILDER.append("Project current xml setup files : ").append("\r\n");
		listCodes(new File("."), blackList, 0, ".xml");
		printSeparator(40);*/
		STRING_BUILDER.append("Project current sources code : ").append("\r\n");
		listCodes(new File("src/main/java/fr/yukina/game/profiling"), blackList, 0, ".java");
		printSeparator(40);

		var myString        = STRING_BUILDER.toString();
		var stringSelection = new StringSelection(myString);
		var clipboard       = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}

	public final static void printSeparator(int lengthIn)
	{
		printSeparator(lengthIn, 0);
	}

	public final static void printSeparator(int lengthIn, int levelIn)
	{
		STRING_BUILDER.append("  ".repeat(levelIn) + "-".repeat(lengthIn)).append("\r\n");
	}

	public final static boolean isValid(File rootIn, String[] blackListIn, String... extensionsIn)
	{
		if (blackListIn != null)
		{
			for (var blackList : blackListIn)
			{
				if (rootIn.getName().contains(blackList))
				{
					return false;
				}
			}
		}

		if (rootIn.getName().equals("Lister.java"))
		{
			return false;
		}

		if (!rootIn.exists() || !rootIn.canRead())
		{
			return false;
		}

		if (rootIn.isFile())
		{
			for (var extension : extensionsIn)
			{
				if (extension.contentEquals("*"))
				{
					return true;
				}

				if (rootIn.getName().endsWith(extension))
				{
					return true;
				}
			}
		}
		else if (rootIn.isDirectory())
		{
			return true;
		}

		return false;
	}

	public final static void listCodes(File rootIn, String[] blackListIn, int levelIn, String... extensionsIn)
	{
		if (!isValid(rootIn, blackListIn, extensionsIn))
		{
			return;
		}

		if (rootIn.isFile())
		{
			STRING_BUILDER.append("  ".repeat(levelIn) + rootIn.getName() + ": ").append("\r\n");
			try (var reader = new BufferedReader(new FileReader(rootIn)))
			{
				String line;
				while ((line = reader.readLine()) != null)
				{
					STRING_BUILDER.append("  ".repeat(levelIn) + "\t" + line).append("\r\n");
				}
			}
			catch (FileNotFoundException eIn)
			{
				throw new RuntimeException(eIn);
			}
			catch (IOException eIn)
			{
				throw new RuntimeException(eIn);
			}
			return;
		}

		STRING_BUILDER.append("  ".repeat(levelIn) + rootIn.getName() + "/").append("\r\n");
		var files = rootIn.listFiles();
		if (files == null)
		{
			return;
		}

		for (var file : files)
		{
			listCodes(file, blackListIn, levelIn + 1, extensionsIn);
		}
	}

	public static final void listFiles(File rootIn, String[] blackListIn, int levelIn, String... extensionsIn)
	{
		if (!isValid(rootIn, blackListIn, extensionsIn))
		{
			return;
		}

		if (rootIn.isFile())
		{
			STRING_BUILDER.append("  ".repeat(levelIn) + rootIn.getName()).append("\r\n");

			return;
		}

		STRING_BUILDER.append("  ".repeat(levelIn) + rootIn.getName() + "/").append("\r\n");

		var files = rootIn.listFiles();
		if (files == null)
		{
			return;
		}

		for (var file : files)
		{
			listFiles(file, blackListIn, levelIn + 1, extensionsIn);
		}
	}
}