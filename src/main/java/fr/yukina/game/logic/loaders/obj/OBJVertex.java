package fr.yukina.game.logic.loaders.obj;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public class OBJVertex
{
	private static final int NO_INDEX = -1;

	private Vector3f  position;
	private int       textureIndex;
	private int       normalIndex;
	private OBJVertex duplicateVertex;
	private int       index;
	private float     length;

	public OBJVertex(int indexIn, Vector3f positionIn)
	{
		this.index        = indexIn;
		this.textureIndex = NO_INDEX;
		this.normalIndex  = NO_INDEX;
		this.position     = positionIn;
		this.length       = positionIn.length();
	}

	public boolean isSet()
	{
		return this.textureIndex != NO_INDEX && this.normalIndex != NO_INDEX;
	}

	public boolean hasSameTextureAndNormal(int textureIndexIn, int normalIndexIn)
	{
		return textureIndexIn == this.textureIndex && normalIndexIn == this.normalIndex;
	}

}