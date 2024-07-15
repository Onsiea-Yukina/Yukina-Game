package fr.yukina.game.logic.terrain.brush;

import fr.yukina.game.logic.terrain.Terrain;

public class SphereBrush extends CursorBrush
{
	public SphereBrush(String nameIn)
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
					var finalX = x + (int) +centerXIn;
					var finalZ = z + (int) +centerZIn;
					if (finalX < 0 || finalX >= Terrain.WIDTH || finalZ < 0 || finalZ >= Terrain.DEPTH)
					{
						continue;
					}
					var finalY    = terrainIn.get(x + centerXIn, z + centerZIn);
					var distanceX = x;
					var distanceY = centerYIn - finalY;
					var distanceZ = z;
					var distance  = Math.sqrt(distanceX * distanceX + distanceY * distanceY + distanceZ * distanceZ);
					if (distance >= radius)
					{
						continue;
					}

					actionIn.apply(terrainIn, finalX, finalY, finalZ, heightCollectorIn);
				}
			}
			return true;
		});
	}
}
