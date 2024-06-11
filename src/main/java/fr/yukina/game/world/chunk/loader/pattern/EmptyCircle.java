package fr.yukina.game.world.chunk.loader.pattern;

public class EmptyCircle implements IPattern
{
	private float distanceX;
	private float distanceZ;
	private float radius;
	private float radiusSquaredMin;
	private float radiusSquaredMax;
	public  float width;
	public  float depth;
	private float length;

	public EmptyCircle(final float radiusIn)
	{
		this.distanceX = radiusIn;
		this.distanceZ = radiusIn;
		this.width     = 1;
		this.depth     = 1;
		this.set(1.0f);
	}

	public EmptyCircle(final float distanceXIn, final float distanceZIn, final float widthIn, final float depthIn)
	{
		this.distanceX = distanceXIn;
		this.distanceZ = distanceZIn;
		this.width     = widthIn;
		this.depth     = depthIn;
		set(1.0f);
	}

	public final void set(final float lengthIn)
	{
		this.length = lengthIn;
		this.radius = (float) Math.sqrt(this.distanceX * this.distanceX + this.distanceZ * this.distanceZ);

		var distanceX = this.distanceX * this.width;
		var distanceZ = this.distanceX * this.depth;
		this.radiusSquaredMin = (distanceX - this.length * this.width) * (distanceX - this.length * this.width)
		                        + (distanceZ - this.length * this.depth) * (distanceZ - this.length * this.depth);
		this.radiusSquaredMax = (distanceX) * (distanceX) + (distanceZ) * (distanceZ);
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

				if (distanceSquared > this.radiusSquaredMax || distanceSquared < this.radiusSquaredMin)
				{
					continue;
				}

				patternManagerIn.add((int) x, (int) z);
			}
		}
	}
}
