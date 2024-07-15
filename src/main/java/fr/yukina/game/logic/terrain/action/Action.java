package fr.yukina.game.logic.terrain.action;

import lombok.Getter;
import lombok.Setter;

public abstract class Action implements IActionFunction
{
	private @Getter         String  name;
	private @Getter         boolean hasChanged;
	private @Getter @Setter boolean canBePrecalculated;

	public Action(String nameIn)
	{
		this.name               = nameIn;
		this.canBePrecalculated = true;
	}
}