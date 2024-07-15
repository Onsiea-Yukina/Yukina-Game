package fr.yukina.game.logic.terrain.picker;

import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import org.joml.Vector3f;

public class BinarySearchStrategy implements IIntersectionStrategy
{
	private static final int   RECURSION_COUNT = 200;
	private static final float RAY_RANGE       = 10.0f;

	@Override
	public boolean findIntersection(Vector3f ray, Terrain terrain, Camera camera, Vector3f intersectionPoint)
	{
		if (intersectionInRange(0, RAY_RANGE, ray, terrain, camera))
		{
			intersectionPoint.set(binarySearch(0, 0, RAY_RANGE, ray, terrain, camera));
			return true;
		}
		return false;
	}

	private Vector3f binarySearch(int count, float start, float finish, Vector3f ray, Terrain terrain, Camera camera)
	{
		float half = start + ((finish - start) / 2.0f);
		if (count >= RECURSION_COUNT)
		{
			return getPointOnRay(ray, half, camera);
		}
		if (intersectionInRange(start, half, ray, terrain, camera))
		{
			return binarySearch(count + 1, start, half, ray, terrain, camera);
		}
		else
		{
			return binarySearch(count + 1, half, finish, ray, terrain, camera);
		}
	}

	private boolean intersectionInRange(float start, float finish, Vector3f ray, Terrain terrain, Camera camera)
	{
		Vector3f startPoint = getPointOnRay(ray, start, camera);
		Vector3f endPoint   = getPointOnRay(ray, finish, camera);
		return !isUnderGround(startPoint, terrain) && isUnderGround(endPoint, terrain);
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