package fr.yukina.game.graphic.window.handler;

import lombok.Getter;
import lombok.Setter;

public class Subscriptable<T> implements ISubscriptable<T>
{
	private                 long           handle;
	private final           T              value;
	private final           ISubscriber<T> subscriber;
	private @Getter @Setter boolean        paused;

	public Subscriptable(T valueIn, ISubscriber<T> subscriberIn, long handleIn)
	{
		this.value      = valueIn;
		this.subscriber = subscriberIn;
		this.paused     = false;
		this.handle     = handleIn;
	}

	public boolean subscribed()
	{
		if (this.handle < 0)
		{
			return false;
		}

		return this.subscriber.subscribed(this.handle);
	}

	@Override
	public void subscribe()
	{
		if (this.subscribed())
		{
			return;
		}

		this.handle = this.subscriber.subscribe(this);
	}

	@Override
	public ISubscriptable<T> pause()
	{
		this.paused = true;

		return this;
	}

	@Override
	public ISubscriptable<T> resume()
	{
		this.paused = false;

		return this;
	}

	@Override
	public void unsubscribe()
	{
		if (!this.subscribed())
		{
			return;
		}

		this.subscriber.unsubscribe(this.handle);
		this.handle = -1;
	}

	@Override
	public T get()
	{
		return this.value;
	}
}
