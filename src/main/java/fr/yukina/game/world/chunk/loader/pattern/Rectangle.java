package fr.yukina.game.world.chunk.loader.pattern;

public class Rectangle implements IPattern
{
	private final int width;
	private final int depth;

	public Rectangle(final int widthIn, final int depthIn)
	{
		this.width = widthIn;
		this.depth = depthIn;
	}

	@Override
	public void set(final PatternManager patternManagerIn)
	{
		for (int x = -this.width; x < this.width; x++)
		{
			for (int z = -this.depth; z < this.depth; z++)
			{
				patternManagerIn.add(x, z);
			}
		}
	}
}
