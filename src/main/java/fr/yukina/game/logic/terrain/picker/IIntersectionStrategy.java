package fr.yukina.game.logic.terrain.picker;

import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import org.joml.Vector3f;

public interface IIntersectionStrategy
{
	boolean findIntersection(Vector3f ray, Terrain terrain, Camera camera, Vector3f intersectionPoint);
}
