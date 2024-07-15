package fr.yukina.game.logic.terrain.picker;

import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import org.joml.Vector3f;

public class RayMarchingStrategy implements IIntersectionStrategy
{
	private static final float RAY_STEP_SIZE = 0.1f;
	private static final float RAY_RANGE     = 10.0f;

	@Override
	public boolean findIntersection(Vector3f ray, Terrain terrain, Camera camera, Vector3f intersectionPoint)
	{
		for (float i = 0; i < RAY_RANGE; i += RAY_STEP_SIZE)
		{
			Vector3f pointOnRay = getPointOnRay(ray, i, camera);
			if (isUnderGround(pointOnRay, terrain))
			{
				intersectionPoint.set(pointOnRay);
				return true;
			}
		}
		return false;
	}

	private boolean isUnderGround(Vector3f testPoint, Terrain terrain)
	{
		if (terrain == null)
		{
			return false;
		}
		float height = terrain.averageGet(testPoint.x, testPoint.z);
		return testPoint.y < height;
	}

	private Vector3f getPointOnRay(Vector3f ray, float distance, Camera camera)
	{
		Vector3f cameraPosition = camera.position();
		Vector3f start          = new Vector3f(cameraPosition);
		Vector3f scaledRay      = new Vector3f(ray).mul(distance);
		return start.add(scaledRay);
	}
}
