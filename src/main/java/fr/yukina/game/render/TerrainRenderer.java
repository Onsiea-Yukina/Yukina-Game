package fr.yukina.game.render;

import fr.yukina.game.render.opengl.GraphicObject;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@Getter
public class TerrainRenderer
{
	private final           Terrain       terrain;
	private final           GraphicObject graphicObject;
	private                 boolean       uploaded;
	private @Getter @Setter boolean       attemptRender;

	public TerrainRenderer(Terrain terrainIn)
	{
		this.terrain       = terrainIn;
		this.graphicObject = new GraphicObject();
	}

	public final void upload(FloatBuffer verticesIn, IntBuffer indicesIn)
	{
		this.graphicObject.attach();
		var vbo = this.graphicObject.bufferObject(GL20.GL_ARRAY_BUFFER, 3, 0, 0);
		vbo.data(verticesIn);
		vbo.detach();

		this.graphicObject.indexBufferObject().data(indicesIn);

		this.graphicObject.detach();

		this.uploaded = true;
		verticesIn.clear();
		indicesIn.clear();
	}

	public void render()
	{
		this.attemptRender(false);
		if (!this.uploaded)
		{
			return;
		}
		this.attemptRender(true);
		this.graphicObject.render();
	}

	public void cleanup()
	{
		this.graphicObject.cleanup();
	}
}