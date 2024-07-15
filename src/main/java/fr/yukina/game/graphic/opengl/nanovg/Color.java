package fr.yukina.game.graphic.opengl.nanovg;

import lombok.Getter;
import org.joml.Vector4f;

@Getter
public class Color
{
	private final Vector4f fill;
	private final Vector4f stroke;

	public Color()
	{
		this(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
	}

	public Color(float fillRIn, float fillGIn, float fillBIn, float fillAIn)
	{
		this(fillRIn, fillGIn, fillBIn, fillAIn, 0.0f, 0.0f, 0.0f, 0.0f);
	}

	public Color(float fillRIn, float fillGIn, float fillBIn, float fillAIn, float strokeRIn, float strokeGIn,
	             float strokeBIn, float strokeAIn)
	{
		this.fill   = new Vector4f(fillRIn, fillGIn, fillBIn, fillAIn);
		this.stroke = new Vector4f(strokeRIn, strokeGIn, strokeBIn, strokeAIn);
	}

	public final Color fill(float rIn, float gIn, float bIn)
	{
		this.fill.set(rIn, gIn, bIn, 1.0f);
		return this;
	}

	public final Color fill(float rIn, float gIn, float bIn, float aIn)
	{
		this.fill.set(rIn, gIn, bIn, aIn);
		return this;
	}

	public final Color stroke(float rIn, float gIn, float bIn)
	{
		this.stroke.set(rIn, gIn, bIn, 1.0f);
		return this;
	}

	public final Color stroke(float rIn, float gIn, float bIn, float aIn)
	{
		this.stroke.set(rIn, gIn, bIn, aIn);
		return this;
	}
}