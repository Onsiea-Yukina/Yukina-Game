package fr.yukina.game.logic.terrain.height;

import lombok.Setter;

public class FlatGenerator implements IHeightProvider
{
	private @Setter float height;

	public FlatGenerator()
	{
		this.height = 0.0f;
	}

	public FlatGenerator(final float heightIn)
	{
		this.height = heightIn;
	}

	@Override
	public float get(final float xIn, final float zIn)
	{
		return this.height;
	}
}