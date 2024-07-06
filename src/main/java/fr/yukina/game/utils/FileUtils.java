package fr.yukina.game.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils
{
	public static String content(String filePathIn) throws Exception
	{
		StringBuilder result = new StringBuilder();
		try (InputStream is = new FileInputStream(filePathIn))
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String         line;
			while ((line = reader.readLine()) != null)
			{
				result.append(line).append("\n");
			}
		}
		return result.toString();
	}

	public static List<String> lines(String filePathIn) throws Exception
	{
		List<String> result = null;
		try (InputStream is = new FileInputStream(filePathIn))
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String         line;
			while ((line = reader.readLine()) != null)
			{
				if (result == null)
				{
					result = new ArrayList<>();
				}
				result.add(line);
			}
		}
		return result;
	}

	public static String computeMD5(Path pathIn) throws IOException, NoSuchAlgorithmException
	{
		byte[]        fileBytes = Files.readAllBytes(pathIn);
		MessageDigest md        = MessageDigest.getInstance("MD5");
		byte[]        digest    = md.digest(fileBytes);
		StringBuilder sb        = new StringBuilder();
		for (byte b : digest)
		{
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
}