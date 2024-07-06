package fr.yukina.game.logic.loaders.obj;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OBJData
{
	private float[] vertices;
	private float[] textureCoords;
	private float[] normals;
	private int[]   indices;
	private float   furthestPoint;

	public OBJData(float[] verticesIn, float[] textureCoordsIn, float[] normalsIn, int[] indicesIn,
	               float furthestPointIn)
	{
		this.vertices      = verticesIn;
		this.textureCoords = textureCoordsIn;
		this.normals       = normalsIn;
		this.indices       = indicesIn;
		this.furthestPoint = furthestPointIn;
	}
}