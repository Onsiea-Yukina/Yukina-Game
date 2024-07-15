package fr.yukina.game.logic.terrain.calculation;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.action.Action;
import fr.yukina.game.logic.terrain.brush.Brush;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import lombok.Getter;
import lombok.Setter;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Getter
public class CalculationChain
{
	private @Setter Calculation  currentCalculation;
	private final   Queue<Apply> applyStack;

	public CalculationChain()
	{
		this.applyStack = new ConcurrentLinkedQueue<>();
	}

	public final void precalculate(Terrain terrainIn, Brush brushIn, Action actionIn)
	{
		if (this.currentCalculation != null)
		{
			this.currentCalculation.stop();
		}
		this.currentCalculation = new Calculation(terrainIn, brushIn, actionIn);
	}

	public final void apply(IHeightCollector destinationIn)
	{
		if (this.currentCalculation == null)
		{
			return;
		}

		var apply = new Apply(this.currentCalculation, destinationIn);
		this.currentCalculation = null;
		apply.calculation().priorize();
		this.applyStack.add(apply);
	}

	public final boolean update()
	{
		int finished     = 0;
		var workingStack = new ConcurrentLinkedQueue<Apply>();
		for (var apply : this.applyStack)
		{
			if (apply.calculation().finished())
			{
				System.out.println("APPLY ! " + apply.calculation().brush().name());
				apply.apply();
				finished++;
				continue;
			}
			workingStack.add(apply);
		}
		this.applyStack.clear();
		this.applyStack.addAll(workingStack);

		return finished > 0;
	}

	public final void stop()
	{
		if (this.currentCalculation != null)
		{
			this.currentCalculation.stop();
		}

		for (var apply : this.applyStack)
		{
			apply.calculation().stop();
		}
		this.applyStack.clear();
	}
}