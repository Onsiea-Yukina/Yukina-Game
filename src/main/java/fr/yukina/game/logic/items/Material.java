package fr.yukina.game.logic.items;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

@Getter
public class Material
{
	private final   Vector4f color;
	private final   Vector4f diffuseColor;
	private final   Vector4f specularColor;
	private @Setter float    reflectance;

	public Material(float reflectanceIn)
	{
		this.color         = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
		this.diffuseColor  = new Vector4f();
		this.specularColor = new Vector4f();
		this.reflectance   = reflectanceIn;
	}
}
