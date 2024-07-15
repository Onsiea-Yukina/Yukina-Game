package fr.yukina.game.logic.terrain.picker;

import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.terrain.Terrain;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class MousePicker
{
	@Getter private final Vector3f              currentRay          = new Vector3f();
	private final         Camera                camera;
	private final         Matrix4f              projectionMatrix;
	private final         GLFWWindow            window;
	private final         Terrain               terrain;
	@Getter private final Vector3f              currentTerrainPoint = new Vector3f();
	@Getter private       boolean               hasTerrainPoint;
	private               Matrix4f              viewMatrix;
	private               IIntersectionStrategy intersectionStrategy;
	private final         Vector4f              mouseRay;
	private final         Matrix4f              invertedViewMatrix;
	private final         Vector3f              worldCoords;
	private final         Matrix4f              invertedProjection;
	private final         Vector4f              eyeCoords;
	private final         Vector2f              normalizedCoords;
	private               long                  last;

	public MousePicker(Camera camera, Matrix4f projectionMatrix, GLFWWindow window, Terrain terrain,
	                   IIntersectionStrategy strategy)
	{
		this.camera               = camera;
		this.projectionMatrix     = new Matrix4f(projectionMatrix);
		this.window               = window;
		this.terrain              = terrain;
		this.viewMatrix           = new Matrix4f(camera.viewMatrix());
		this.intersectionStrategy = strategy;

		this.mouseRay           = new Vector4f();
		this.invertedViewMatrix = new Matrix4f();
		this.worldCoords        = new Vector3f();
		this.invertedProjection = new Matrix4f();
		this.eyeCoords          = new Vector4f();
		this.normalizedCoords   = new Vector2f();
		this.last               = System.nanoTime();
	}

	public void setIntersectionStrategy(IIntersectionStrategy strategy)
	{
		this.intersectionStrategy = strategy;
	}

	public boolean update()
	{
		if (!this.camera.updateState().hasChanged())
		{
			return false;
		}

		if (System.nanoTime() - this.last > 1_000_000L)
		{
			this.viewMatrix.set(camera.viewMatrix());
			this.currentRay.set(calculateMouseRay());
			this.hasTerrainPoint = intersectionStrategy.findIntersection(currentRay, terrain, camera,
			                                                             currentTerrainPoint);

			this.last = System.nanoTime();

			return true;
		}

		return false;
	}

	private Vector3f calculateMouseRay()
	{
		Vector2f normalizedCoords = toNormalizedDeviceCoords(window.mousePosition());
		Vector4f clipCoords       = this.mouseRay.set(normalizedCoords.x, normalizedCoords.y, -1.0f, 1.0f);
		Vector4f eyeCoords        = toEyeCoords(clipCoords);
		return toWorldCoords(eyeCoords);
	}

	private Vector3f toWorldCoords(Vector4f eyeCoords)
	{
		Vector4f rayWorld = viewMatrix.invert(this.invertedViewMatrix).transform(eyeCoords);
		Vector3f mouseRay = this.worldCoords.set(rayWorld.x, rayWorld.y, rayWorld.z);
		return mouseRay.normalize();
	}

	private Vector4f toEyeCoords(Vector4f clipCoords)
	{
		Matrix4f invertedProjection = projectionMatrix.invert(this.invertedProjection);
		Vector4f eyeCoords          = invertedProjection.transform(clipCoords);
		return this.eyeCoords.set(eyeCoords.x, eyeCoords.y, -1.0f, 0.0f);
	}

	private Vector2f toNormalizedDeviceCoords(double[] mousePosition)
	{
		float x = (2.0f * (float) mousePosition[0]) / window.width() - 1.0f;
		float y = 1.0f - (2.0f * (float) mousePosition[1]) / window.height();
		return this.normalizedCoords.set(x, y);
	}
}