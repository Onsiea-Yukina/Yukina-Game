package fr.yukina;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.glTexImage3D;
import static org.lwjgl.opengl.GL12.glTexSubImage3D;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class TextureArrayLoader
{

	public static int loadTextureArray(List<String> textureFiles)
	{
		int textureArrayId = glGenTextures();
		glBindTexture(GL_TEXTURE_2D_ARRAY, textureArrayId);

		int          width   = 0, height = 0;
		ByteBuffer[] buffers = new ByteBuffer[textureFiles.size()];

		for (int i = 0; i < textureFiles.size(); i++)
		{
			try (MemoryStack stack = MemoryStack.stackPush())
			{
				IntBuffer w    = stack.mallocInt(1);
				IntBuffer h    = stack.mallocInt(1);
				IntBuffer comp = stack.mallocInt(1);

				if (!Files.exists(Paths.get(textureFiles.get(i))))
				{
					throw new RuntimeException("Texture file not found: " + textureFiles.get(i));
				}

				buffers[i] = STBImage.stbi_load(textureFiles.get(i), w, h, comp, 4);
				if (buffers[i] == null)
				{
					throw new RuntimeException("Failed to load texture file " + textureFiles.get(i) + "\n"
					                           + STBImage.stbi_failure_reason());
				}

				if (i == 0)
				{
					width  = w.get();
					height = h.get();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException("Failed to load texture file: " + textureFiles.get(i));
			}
		}

		glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA8, width, height, textureFiles.size(), 0, GL_RGBA,
		             GL_UNSIGNED_BYTE,
		             (ByteBuffer) null);

		for (int i = 0; i < textureFiles.size(); i++)
		{
			glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, width, height, 1, GL_RGBA, GL_UNSIGNED_BYTE, buffers[i]);
			STBImage.stbi_image_free(buffers[i]);
		}

		glGenerateMipmap(GL_TEXTURE_2D_ARRAY);

		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

		return textureArrayId;
	}
}