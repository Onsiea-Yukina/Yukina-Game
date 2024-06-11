package fr.yukina.game.world.chunk.loader.pattern;

public class Square implements IPattern
{
	private final int size;

	public Square(final int sizeIn)
	{
		this.size = sizeIn;
	}

	@Override
	public void set(final PatternManager patternManagerIn)
	{
		for (int x = -this.size; x < this.size; x++)
		{
			for (int z = -this.size; z < this.size; z++)
			{
				patternManagerIn.add(x, z);
			}
		}
	}
}
