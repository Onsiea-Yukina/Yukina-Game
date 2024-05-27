package fr.yukina.game;

import fr.yukina.game.window.IWindow;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

@Getter
public class FPSCamera implements IGameLogic
{
	private final   Vector3f position;
	private final   Vector3f rotation;
	private final   Matrix4f projectionMatrix;
	private final   Matrix4f viewMatrix;
	private final   Matrix4f projectionViewMatrix;
	private @Setter boolean  projectionViewMatrixHasChanged;
	private final   float    fov;
	private final   float    aspectRatio;
	private final   float    nearPlane;
	private final   float    farPlane;

	public FPSCamera(float fovIn, float aspectRatioIn, float nearPlaneIn, float farPlaneIn)
	{
		this.position             = new Vector3f();
		this.rotation             = new Vector3f();
		this.projectionMatrix     = new Matrix4f().perspective(fovIn, aspectRatioIn, nearPlaneIn, farPlaneIn);
		this.viewMatrix           = new Matrix4f();
		this.projectionViewMatrix = new Matrix4f();
		this.updateViewMatrix();
		this.fov                            = fovIn;
		this.aspectRatio                    = aspectRatioIn;
		this.nearPlane                      = nearPlaneIn;
		this.farPlane                       = farPlaneIn;
		this.projectionViewMatrixHasChanged = false;
	}

	public void updateViewMatrix()
	{
		this.viewMatrix.identity().rotateX((float) Math.toRadians(this.rotation.x))
		               .rotateY((float) Math.toRadians(this.rotation.y))
		               .translate(-this.position.x, -this.position.y, -this.position.z);

		this.projectionViewMatrix.set(this.projectionMatrix).mul(this.viewMatrix);

		this.projectionViewMatrixHasChanged = true;
	}

	@Override
	public void initialize()
	{
	}

	@Override
	public void input(final IWindow windowIn)
	{
	}

	@Override
	public void update()
	{
	}

	@Override
	public void cleanup()
	{
	}
}