package fr.yukina.game.world.chunk;

import fr.yukina.game.world.terrain.Terrain;
import org.joml.Vector3f;

public interface IChunk
{
	String key();

	Terrain terrain();

	int frustumUpdateFrame();

	boolean visible();

	IChunk visible(int frustumUpdateFrameIn, boolean visibleIn);

	boolean needUnload();

	IChunk needUnload(boolean needUnloadIn);

	Vector3f min();

	Vector3f max();

	void generate();

	void cleanup();
}