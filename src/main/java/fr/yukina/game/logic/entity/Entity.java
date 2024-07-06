package fr.yukina.game.logic.entity;

import fr.yukina.game.logic.items.GameItem;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.joml.Vector3f;

@Getter
public class Entity
{
	private final @Delegate GameItem gameItem;
	private final           Vector3f velocity;
	private final           Vector3f acceleration;
	private final           Vector3f orientationVelocity; // in degrees
	private final           Vector3f orientationAcceleration; // in degrees

	public Entity(final GameItem gameItemIn)
	{
		this.gameItem                = gameItemIn;
		this.velocity                = new Vector3f();
		this.acceleration            = new Vector3f();
		this.orientationVelocity     = new Vector3f();
		this.orientationAcceleration = new Vector3f();
	}

	public void update()
	{
		this.gameItem.position().add(this.velocity);
		this.gameItem.orientation().add(this.orientationVelocity);
		this.velocity.add(this.acceleration);
		this.orientationVelocity.add(this.orientationAcceleration);
	}
}