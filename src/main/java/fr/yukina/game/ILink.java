package fr.yukina.game;

public interface ILink<L>
{
	L link();

	ILink<L> link(L value);
}