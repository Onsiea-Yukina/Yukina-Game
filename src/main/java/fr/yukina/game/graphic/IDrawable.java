package fr.yukina.game.graphic;

import fr.yukina.game.logic.items.GameItem;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.function.Consumer;

public interface IDrawable
{
	void attach();

	default void draw(Collection<GameItem> gameItemsIn, Consumer<GameItem> consumerIn)
	{
		this.attach();
		for (var gameItem : gameItemsIn)
		{
			consumerIn.accept(gameItem);
			this.draw(gameItem.position(), gameItem.orientation(), gameItem.scale());
		}
		this.detach();
	}

	void draw(Vector3f positionIn, Vector3f orientationIn, Vector3f scaleIn);

	void detach();
}