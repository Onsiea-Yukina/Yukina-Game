package fr.yukina.game.logic.terrain.calculation;

import fr.yukina.game.logic.terrain.TerrainEditor;
import fr.yukina.game.utils.IFunction;
import lombok.Setter;

public class Precalculator
{
	private final   TerrainEditor    terrainEditor;
	private final   CalculationChain calculationChain;
	private @Setter long             updateTime;
	private         long             last;
	private @Setter IFunction        applyFinishedFunction;

	public Precalculator(TerrainEditor terrainEditor)
	{
		this.terrainEditor    = terrainEditor;
		this.calculationChain = new CalculationChain();
		this.updateTime       = 5_000L;
		this.last             = System.nanoTime();
	}

	public final void precalculate()
	{
		this.calculationChain.precalculate(this.terrainEditor.terrain(), this.terrainEditor.brush(),
		                                   this.terrainEditor.action());
	}

	public final void apply()
	{
		this.calculationChain.apply((xIn, zIn, heightIn) ->
		                            {
			                            this.terrainEditor.terrain().set(xIn, zIn, heightIn);
		                            });
	}

	public final void update()
	{
		if (System.nanoTime() - this.last < this.updateTime)
		{
			return;
		}
		this.last = System.nanoTime();

		if (this.calculationChain.update() && this.applyFinishedFunction != null)
		{
			this.applyFinishedFunction.execute();
		}
	}

	public final void stop()
	{
		this.calculationChain.stop();
	}
}