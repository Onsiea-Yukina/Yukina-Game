package fr.yukina.game.world.chunk.loader.pattern;

public class Circle implements IPattern
{
	private float radius;
	private float radiusSquared;
	public  float width;
	public  float depth;

	public Circle(final float radiusIn)
	{
		this.radius        = radiusIn;
		this.radiusSquared = radiusIn * radiusIn;
		this.width         = 1;
		this.depth         = 1;
	}

	public Circle(final float distanceXIn, final float distanceZIn, final float widthIn, final float depthIn)
	{
		this.radius        = (float) Math.sqrt(distanceXIn * distanceXIn + distanceZIn * distanceZIn);
		this.radiusSquared =
				distanceXIn * widthIn * distanceXIn * widthIn + distanceZIn * depthIn * distanceZIn * depthIn;
		this.width         = widthIn;
		this.depth         = depthIn;
	}

	public final void updateRadius(final float distanceXIn, final float distanceZIn)
	{
		this.radius        = (float) Math.sqrt(distanceXIn * distanceXIn + distanceZIn * distanceZIn);
		this.radiusSquared = distanceXIn * this.width * distanceXIn * this.width
		                     + distanceZIn * this.depth * distanceZIn * this.depth;
		this.width         = this.width;
		this.depth         = this.depth;
	}

	@Override
	public void set(final PatternManager patternManagerIn)
	{
		for (float x = -this.radius; x < this.radius; x++)
		{
			for (float z = -this.radius; z < this.radius; z++)
			{
				var   width           = x * this.width;
				var   depth           = z * this.depth;
				float distanceSquared = width * width + depth * depth;

				if (distanceSquared > this.radiusSquared)
				{
					continue;
				}

				patternManagerIn.add((int) x, (int) z);
			}
		}
	}
}
