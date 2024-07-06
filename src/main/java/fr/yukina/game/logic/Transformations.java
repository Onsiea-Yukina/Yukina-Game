package fr.yukina.game.logic;

import fr.yukina.game.logic.entity.Entity;
import fr.yukina.game.logic.items.GameItem;
import org.joml.Matrix4f;

public class Transformations
{
	private final static Matrix4f MATRIX = new Matrix4f();

	public final static Matrix4f of(GameItem gameItemIn)
	{
		MATRIX.identity();
		MATRIX.translate(gameItemIn.position().x, gameItemIn.position().y, gameItemIn.position().z);
		MATRIX.rotateX(gameItemIn.xOrientationRad());
		MATRIX.rotateY(gameItemIn.yOrientationRad());
		MATRIX.rotateZ(gameItemIn.zOrientationRad());
		MATRIX.scale(gameItemIn.scale().x, gameItemIn.scale().y, gameItemIn.scale().z);

		return MATRIX;
	}

	public final static Matrix4f of(Entity entityIn)
	{
		MATRIX.identity();
		MATRIX.translate(entityIn.position().x, entityIn.position().y, entityIn.position().z);
		MATRIX.rotateX(entityIn.xOrientationRad());
		MATRIX.rotateY(entityIn.yOrientationRad());
		MATRIX.rotateZ(entityIn.zOrientationRad());
		MATRIX.scale(entityIn.scale().x, entityIn.scale().y, entityIn.scale().z);

		return MATRIX;
	}
}
