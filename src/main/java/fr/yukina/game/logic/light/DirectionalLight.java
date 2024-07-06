package fr.yukina.game.logic.light;

import lombok.Getter;
import org.joml.Vector3f;

@Getter
public class DirectionalLight extends Light
{
	private Vector3f direction;

	public DirectionalLight(Vector3f directionIn, Vector3f colorIn)
	{
		super(new Vector3f(0, 0, 0), colorIn);
		this.direction = directionIn;
	}
}