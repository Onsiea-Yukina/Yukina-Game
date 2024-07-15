package fr.yukina.game.logic.terrain.action;

import fr.yukina.game.logic.terrain.Terrain;
import fr.yukina.game.logic.terrain.height.IHeightCollector;
import lombok.Getter;
import lombok.Setter;

public class Smooth extends Action
{
	private @Setter @Getter float increment;

	public Smooth(String nameIn)
	{
		super(nameIn);
		this.increment = 0.125f;
	}

	public final void apply(Terrain terrainIn, float xIn, float yIn, float zIn, IHeightCollector heightCollectorIn)
	{
		var radius = 4.0f;
		var total  = 0.0f;
		var count  = 0;
		for (var x = -radius; x < radius; x++)
		{
			for (var z = -radius; z < radius; z++)
			{
				if (x == 0.0f && z == 0.0f)
				{
					continue;
				}
				var distanceX = x;
				var distanceZ = z;
				var distance  = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);
				if (distance >= radius)
				{
					continue;
				}
				total += terrainIn.get(Math.clamp(x + xIn, 0, Terrain.WIDTH - 1),
				                       Math.clamp(z + zIn, 0, Terrain.DEPTH - 1));
				count++;
			}
		}
		var weight  = 0.00001f;
		var average = (total / count) * weight;

		float center       = terrainIn.get(xIn, zIn);
		float centerWeight = 1.0f - (weight * count);
		float height       = Math.clamp(average + center * centerWeight, 0.05f, 32.0f);

		heightCollectorIn.collect(xIn, zIn, height);
	}
}