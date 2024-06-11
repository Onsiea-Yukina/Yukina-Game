package fr.yukina.game.world;

import fr.yukina.game.Player;
import fr.yukina.game.world.chunk.IChunk;
import fr.yukina.game.world.terrain.Terrain;
import lombok.Getter;

import java.util.logging.Logger;

@Getter
public class RenderDistance
{
	private static final Logger LOGGER = Logger.getLogger(RenderDistance.class.getName());

	private final int     max;
	private final long    increaseTime;
	private final float   increaseDiv;
	private final Player  player;
	private       float   last;
	private       float   current;
	private       double  blockX;
	private       double  blockZ;
	private       double  blockXZ;
	private       boolean hasChanged;
	private       long    lastIncreaseTime;

	public RenderDistance(final int maxRenderDistanceIn, final long increaseTimeIn, final float increaseDivIn,
	                      final Player playerIn)
	{
		this.max          = maxRenderDistanceIn;
		this.increaseTime = increaseTimeIn;
		this.increaseDiv  = increaseDivIn;
		this.player       = playerIn;

		this.current(this.canIncrease() ? this.max / this.increaseDiv : this.max);
		this.lastIncreaseTime = System.nanoTime();
	}

	public final void current(float renderDistanceIn)
	{
		if (last == renderDistanceIn)
		{
			return;
		}

		this.current = renderDistanceIn;
		if (this.current > this.max)
		{
			this.current = this.max;
		}

		if (this.last == this.current)
		{
			return;
		}
		this.last       = this.current;
		this.hasChanged = true;
		this.calculateDistances();
	}

	private void calculateDistances()
	{
		this.blockX  = (this.current * Terrain.WIDTH);
		this.blockZ  = (this.current * Terrain.DEPTH);
		this.blockXZ = this.blockX * this.blockX + this.blockZ * this.blockZ;
	}

	public final void update()
	{
		this.hasChanged = false;
		if (!this.canIncrease())
		{
			return;
		}

		if (System.nanoTime() - this.lastIncreaseTime >= this.increaseTime)
		{
			this.current(this.current + this.max / this.increaseDiv);
			this.lastIncreaseTime = System.nanoTime();
		}
	}

	public final boolean canIncrease()
	{
		return this.increaseTime >= 1L && this.increaseDiv > 0.0f;
	}

	public boolean isOut(IChunk chunkIn)
	{
		return this.isOut(chunkIn.terrain().x(), chunkIn.terrain().z());
	}

	public boolean isOut(float xIn, float zIn)
	{
		double centerX  = this.player.camera().position().x;
		double centerZ  = this.player.camera().position().z;
		double distX    = centerX - xIn;
		double distZ    = centerZ - zIn;
		double distance = distX * distX + distZ * distZ;

		LOGGER.info(
				String.format("Checking render distance for chunk at (%.2f, %.2f): distance = %.2f, threshold = %.2f",
				              xIn, zIn, distance, this.blockXZ));

		return distance > this.blockXZ;
	}
}