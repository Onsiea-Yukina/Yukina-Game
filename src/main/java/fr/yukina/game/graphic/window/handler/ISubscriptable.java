package fr.yukina.game.graphic.window.handler;

public interface ISubscriptable<T>
{
	boolean paused();

	void subscribe();

	ISubscriptable<T> pause();

	ISubscriptable<T> resume();

	void unsubscribe();

	T get();
}