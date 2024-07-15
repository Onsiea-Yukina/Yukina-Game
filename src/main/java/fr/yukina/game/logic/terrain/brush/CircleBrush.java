package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.logic.terrain.Terrain;

public class CircleBrush extends CursorBrush
{
	public CircleBrush(String nameIn)
	{
		super(nameIn, (terrainIn, canContinueIn, actionIn, centerXIn, centerYIn, centerZIn, heightCollectorIn) ->
		{
			var radius = 4.0f;
			for (var x = -radius; x < radius; x++)
			{
				if (!canContinueIn.execute())
				{
					return false;
				}
				for (var z = -radius; z < radius; z++)
				{
					if (!canContinueIn.execute())
					{
						return false;
					}
					var distanceX = x;
					var distanceZ = z;
					var distance  = Math.sqrt(distanceX * distanceX + distanceZ * distanceZ);
					if (distance >= radius)
					{
						continue;
					}
					var finalX = x + (int) (centerXIn);
					var finalZ = z + (int) (centerZIn);
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
