package fr.yukina.game.logic.light;

import lombok.Getter;
import org.joml.Vector3f;

@Getter
public class PointLight extends Light
{
	private float       intensity;
	private Attenuation attenuation;

	public PointLight(Vector3f positionIn, Vector3f colorIn, float intensityIn, Attenuation attenuationIn)
	{
		super(positionIn, colorIn);
		this.intensity   = intensityIn;
		this.attenuation = attenuationIn;
	}
}