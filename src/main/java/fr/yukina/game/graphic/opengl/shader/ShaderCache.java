package fr.yukina.game.graphic.opengl.shader;

import fr.yukina.game.utils.FileUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class ShaderCache
{
	private static final String CACHE_DIR = "gamedata/shader_cache/";

	static
	{
		final var dir = Paths.get(CACHE_DIR);
		if (!Files.exists(dir))
		{
			try
			{
				Files.createDirectories(dir);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public static void saveShaderToCache(Map<Integer, String> shaderFilePaths, ByteBuffer binary, int format,
	                                     boolean saveShaderCode) throws IOException, NoSuchAlgorithmException
	{
		Path cachePath = getCachePath(shaderFilePaths);
		Files.createDirectories(cachePath.getParent());

		try (FileOutputStream fos = new FileOutputStream(cachePath.toFile());
		     ObjectOutputStream oos = new ObjectOutputStream(fos))
		{
			byte[] bytes = new byte[binary.remaining()];
			binary.get(bytes);
			oos.writeInt(format);
			oos.writeInt(bytes.length);
			oos.write(bytes);
		}

		if (saveShaderCode)
		{
			for (Map.Entry<Integer, String> entry : shaderFilePaths.entrySet())
			{
				saveShaderCode(entry.getValue());
			}
		}

		// Save metadata
		saveMetadata(shaderFilePaths);
	}

	public static ShaderProgramBinary loadProgramBinaryFromCache(Map<Integer, String> shaderFilePaths)
	throws IOException, NoSuchAlgorithmException
	{
		if (!isCacheValid(shaderFilePaths))
		{
			return null;
		}

		Path cachePath = getCachePath(shaderFilePaths);
		try (FileInputStream fis = new FileInputStream(cachePath.toFile());
		     ObjectInputStream ois = new ObjectInputStream(fis))
		{
			int    format = ois.readInt();
			int    length = ois.readInt();
			byte[] bytes  = new byte[length];
			ois.readFully(bytes);

			ByteBuffer binary = ByteBuffer.allocateDirect(bytes.length);
			binary.put(bytes);
			binary.flip();

			return new ShaderProgramBinary(binary, format);
		}
	}

	private static Path getCachePath(Map<Integer, String> shaderFilePaths)
	{
		String cacheFileName = shaderFilePaths.values().iterator().next().replace("/", "_").replace("\\", "_") +
		                       ".bin";
		return Paths.get(CACHE_DIR, cacheFileName);
	}

	private static Path getMetadataPath(Map<Integer, String> shaderFilePaths)
	{
		String metaFileName = shaderFilePaths.values().iterator().next().replace("/", "_").replace("\\", "_") +
		                      ".meta";
		return Paths.get(CACHE_DIR, metaFileName);
	}

	private static void saveMetadata(Map<Integer, String> shaderFilePaths) throws IOException, NoSuchAlgorithmException
	{
		Path          metaPath = getMetadataPath(shaderFilePaths);
		StringBuilder metadata = new StringBuilder();
		for (String filePath : shaderFilePaths.values())
		{
			Path                shaderPath = Paths.get(filePath);
			BasicFileAttributes attr       = Files.readAttributes(shaderPath, BasicFileAttributes.class);

			String md5 = FileUtils.computeMD5(shaderPath);
			metadata.append(attr.size()).append("\n").append(attr.lastModifiedTime().toMillis()).append("\n")
			        .append(md5).append("\n");
		}

		Files.writeString(metaPath, metadata.toString(), StandardCharsets.UTF_8);
	}

	private static void saveShaderCode(String shaderFilePath) throws IOException
	{
		Path sourcePath = Paths.get(shaderFilePath);
		Path destPath   = Paths.get(CACHE_DIR, sourcePath.getFileName().toString() + ".gz");

		try (FileInputStream fis = new FileInputStream(sourcePath.toFile());
		     FileOutputStream fos = new FileOutputStream(destPath.toFile());
		     GZIPOutputStream gzos = new GZIPOutputStream(fos))
		{
			byte[] buffer = new byte[1024];
			int    length;
			while ((length = fis.read(buffer)) > 0)
			{
				gzos.write(buffer, 0, length);
			}
		}
	}

	private static boolean isCacheValid(Map<Integer, String> shaderFilePaths)
	throws IOException, NoSuchAlgorithmException
	{
		Path metaPath = getMetadataPath(shaderFilePaths);
		if (!Files.exists(metaPath))
		{
			return false;
		}

		String[] metadata = Files.readString(metaPath).split("\n");
		int      index    = 0;
		for (String filePath : shaderFilePaths.values())
		{
			long   cachedSize         = Long.parseLong(metadata[index++]);
			long   cachedLastModified = Long.parseLong(metadata[index++]);
			String cachedMD5          = metadata[index++];

			Path                shaderPath = Paths.get(filePath);
			BasicFileAttributes attr       = Files.readAttributes(shaderPath, BasicFileAttributes.class);

			String currentMD5 = FileUtils.computeMD5(shaderPath);

			if (cachedSize != attr.size() || cachedLastModified != attr.lastModifiedTime().toMillis()
			    || !cachedMD5.equals(currentMD5))
			{
				return false;
			}
		}

		return true;
	}

	public static void clearCache()
	{
		try
		{
			Files.walk(Paths.get(CACHE_DIR)).sorted(Comparator.reverseOrder()).forEach((fileIn) ->
			                                                                           {
				                                                                           try
				                                                                           {
					                                                                           Files.delete(fileIn);
				                                                                           }
				                                                                           catch (IOException e)
				                                                                           {
					                                                                           e.printStackTrace();
				                                                                           }
			                                                                           });
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}