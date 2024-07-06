package fr.yukina.game.logic.light;

import lombok.Getter;
import org.joml.Vector3f;

@Getter
public class Light
{
	protected Vector3f position;
	protected Vector3f color;

	public Light(Vector3f positionIn, Vector3f colorIn)
	{
		this.position = positionIn;
		this.color    = colorIn;
	}
}