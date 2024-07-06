package fr.yukina.game.logic.items;

import lombok.Getter;
import org.joml.Vector3f;

@Getter
public class GameItem
{
	private final @Getter Material material;
	private final         Vector3f position;
	private final         Vector3f orientation; // orientation in degrees
	private final         Vector3f scale;

	public GameItem(Material materialIn)
	{
		this.material    = materialIn;
		this.position    = new Vector3f();
		this.orientation = new Vector3f();
		this.scale       = new Vector3f();
	}

	public GameItem(Material materialIn, Vector3f positionIn, Vector3f orientationIn, Vector3f scaleIn)
	{
		this.material    = materialIn;
		this.position    = positionIn;
		this.orientation = orientationIn;
		this.scale       = scaleIn;
	}

	public GameItem(Material materialIn, float xIn, float yIn, float zIn, float xOrientationIn, float yOrientationIn,
	                float zOrientationIn, float xScaleIn, float yScaleIn, float zScaleIn)
	{
		this.material    = materialIn;
		this.position    = new Vector3f(xIn, yIn, zIn);
		this.orientation = new Vector3f(xOrientationIn, yOrientationIn, zOrientationIn);
		this.scale       = new Vector3f(xScaleIn, yScaleIn, zScaleIn);
	}

	public GameItem(Material materialIn, float xIn, float yIn, float zIn)
	{
		this.material    = materialIn;
		this.position    = new Vector3f(xIn, yIn, zIn);
		this.orientation = new Vector3f();
		this.scale       = new Vector3f();
	}

	public final GameItem move(float xIn, float yIn, float zIn)
	{
		this.position.x += xIn;
		this.position.y += yIn;
		this.position.z += zIn;
		return this;
	}

	public final GameItem move(Vector3f positionIn)
	{
		this.position.x += positionIn.x;
		this.position.y += positionIn.y;
		this.position.z += positionIn.z;
		return this;
	}

	public final GameItem position(float xIn, float yIn, float zIn)
	{
		this.position.x = xIn;
		this.position.y = yIn;
		this.position.z = zIn;
		return this;
	}

	public final GameItem position(Vector3f positionIn)
	{
		this.position.x = positionIn.x;
		this.position.y = positionIn.y;
		this.position.z = positionIn.z;
		return this;
	}

	public final GameItem rotate(float xIn, float yIn, float zIn)
	{
		this.orientation.x += xIn;
		this.orientation.y += yIn;
		this.orientation.z += zIn;
		return this;
	}

	public final GameItem rotate(Vector3f orientationIn)
	{
		this.orientation.x += orientationIn.x;
		this.orientation.y += orientationIn.y;
		this.orientation.z += orientationIn.z;
		return this;
	}

	public final GameItem orientation(float xIn, float yIn, float zIn)
	{
		this.orientation.x = xIn;
		this.orientation.y = yIn;
		this.orientation.z = zIn;
		return this;
	}

	public final GameItem orientation(Vector3f orientationIn)
	{
		this.orientation.x = orientationIn.x;
		this.orientation.y = orientationIn.y;
		this.orientation.z = orientationIn.z;
		return this;
	}

	public final float xOrientationRad()
	{
		return (float) Math.toRadians(this.orientation.x);
	}

	public final float yOrientationRad()
	{
		return (float) Math.toRadians(this.orientation.y);
	}

	public final float zOrientationRad()
	{
		return (float) Math.toRadians(this.orientation.z);
	}

	public final GameItem decreaseScale(float scaleIn)
	{
		this.scale.x -= scaleIn;
		this.scale.y -= scaleIn;
		this.scale.z -= scaleIn;
		return this;
	}

	public final GameItem decreaseScale(float xIn, float yIn, float zIn)
	{
		this.scale.x -= xIn;
		this.scale.y -= yIn;
		this.scale.z -= zIn;
		return this;
	}

	public final GameItem decreaseScale(Vector3f scaleIn)
	{
		this.scale.x -= scaleIn.x;
		this.scale.y -= scaleIn.y;
		this.scale.z -= scaleIn.z;
		return this;
	}

	public final GameItem increaseScale(float scaleIn)
	{
		this.scale.x += scaleIn;
		this.scale.y += scaleIn;
		this.scale.z += scaleIn;
		return this;
	}

	public final GameItem increaseScale(float xIn, float yIn, float zIn)
	{
		this.scale.x += xIn;
		this.scale.y += yIn;
		this.scale.z += zIn;
		return this;
	}

	public final GameItem increaseScale(Vector3f scaleIn)
	{
		this.scale.x += scaleIn.x;
		this.scale.y += scaleIn.y;
		this.scale.z += scaleIn.z;
		return this;
	}

	public final GameItem scale(float scaleIn)
	{
		this.scale.x = scaleIn;
		this.scale.y = scaleIn;
		this.scale.z = scaleIn;
		return this;
	}

	public final GameItem scale(float xIn, float yIn, float zIn)
	{
		this.scale.x = xIn;
		this.scale.y = yIn;
		this.scale.z = zIn;
		return this;
	}

	public final GameItem scale(Vector3f scaleIn)
	{
		this.scale.x = scaleIn.x;
		this.scale.y = scaleIn.y;
		this.scale.z = scaleIn.z;
		return this;
	}
}