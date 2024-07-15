package fr.yukina.game.graphic.window.handler;

public interface ISubscriber<T>
{
	ISubscriptable<T> subscribe(final T valueIn);

	ISubscriptable<T> subscribe(final T valueIn, final Priority priorityIn);

	long subscribe(final ISubscriptable<T> subscriptableIn);

	long subscribe(final ISubscriptable<T> subscriptableIn, final Priority priorityIn);

	void unsubscribe(final long handleIn);

	boolean subscribed(long handleIn);
}