package fr.yukina.game.logic.scene;

import fr.yukina.game.graphic.opengl.GLMesh;
import fr.yukina.game.graphic.window.GLFWWindow;
import fr.yukina.game.logic.entity.Entity;
import fr.yukina.game.logic.items.GameItem;
import fr.yukina.game.logic.light.Attenuation;
import fr.yukina.game.logic.light.DirectionalLight;
import fr.yukina.game.logic.light.PointLight;
import fr.yukina.game.logic.player.Camera;
import fr.yukina.game.logic.player.Player;
import lombok.Getter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Getter
public class Scene
{
	private final Map<GLMesh, Collection<GameItem>> items;
	private final Map<GLMesh, Collection<Entity>>   entities;
	private final DirectionalLight                  directionalLight;
	private final PointLight                        pointLight;
	private final Player                            player;
	private final long                              start = System.nanoTime();

	public Scene()
	{
		this.items            = new HashMap<>();
		this.entities         = new HashMap<>();
		this.directionalLight = new DirectionalLight(new Vector3f(0.0f, -1.0f, -1.0f), new Vector3f(1.0f, 1.0f, 1.0f));
		this.pointLight       = new PointLight(new Vector3f(8.0f, 8.0f, 8.0f), new Vector3f(1.0f, 1.0f, 0.0f), 0.075f,
		                                       new Attenuation(0.0f, 0.025f, 0.0000125f));
		this.player           = new Player(new Camera(0.0f, 0.0f, 0.0f, 0.1f, 1000.0f, Math.toRadians(120.0f),
		                                              21 / 9));
	}

	public final void add(GLMesh meshIn, GameItem... itemsIn)
	{
		var items = this.items.get(meshIn);
		if (items == null)
		{
			items = new ArrayList<>();
			this.items.put(meshIn, items);
		}

		for (GameItem item : itemsIn)
		{
			items.add(item);
		}
	}

	public final void add(GLMesh meshIn, Entity... entitiesIn)
	{
		var entities = this.entities.get(meshIn);
		if (entities == null)
		{
			entities = new ArrayList<>();
			this.entities.put(meshIn, entities);
		}

		for (Entity entity : entitiesIn)
		{
			entities.add(entity);
		}
	}

	public final void update(GLFWWindow windowIn)
	{
		this.player.update(windowIn);
		for (var entry : this.items().entrySet())
		{
			for (var item : entry.getValue())
			{
				item.move(0.0f, -0.098f, 0.0f);
			}
		}

		for (var entry : this.entities().entrySet())
		{
			for (var entity : entry.getValue())
			{
				entity.update();
				entity.rotate(0.25f, 0.0f, 0.25f);
			}
		}
	}
}
