package fr.yukina.game.logic.loaders.obj;

import fr.yukina.game.graphic.opengl.GLMesh;

public class OBJUtils
{
	public final static GLMesh toMesh(OBJData dataIn)
	{
		var mesh = new GLMesh();
		mesh.attach();
		mesh.upload(3, dataIn.vertices());
		mesh.upload(2, dataIn.textureCoords());
		mesh.upload(3, dataIn.normals());
		mesh.indices(dataIn.indices());
		mesh.detach();
		return mesh;
	}
}
