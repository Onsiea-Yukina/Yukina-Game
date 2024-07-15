package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.logic.terrain.Terrain;

public class RandomBrush extends CursorBrush
{
	public RandomBrush(String nameIn)
	{
		super(nameIn, (terrainIn, canContinueIn, actionIn, centerXIn, centerYIn, centerZIn, heightCollectorIn) ->
		{
			for (var x = 0; x < Terrain.WIDTH; x++)
			{
				if (!canContinueIn.execute())
				{
					return false;
				}
				for (var z = 0; z < Terrain.DEPTH; z++)
				{
					if (!canContinueIn.execute())
					{
						return false;
					}
					if (Math.random() >= 0.5f)
					{
						continue;
					}

					var finalX = x + (int) centerXIn;
					var finalZ = z + (int) centerZIn;
					if (finalX < 0 || finalX >= Terrain.WIDTH || finalZ < 0 || finalZ >= Terrain.DEPTH)
					{
						continue;
					}
					var finalY = terrainIn.get(finalX, finalZ);

					actionIn.apply(terrainIn, finalX, finalY, finalZ, heightCollectorIn);
				}
			}
			return true;
		});
	}
}