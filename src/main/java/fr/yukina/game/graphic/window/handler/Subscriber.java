package fr.yukina.game.graphic.window.handler;

import lombok.Getter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

public class Subscriber<T> implements ISubscriber<T>, Iterable<T>
{
	private final @Getter Map<Long, ISubscriptable<T>> highSubscriptables;
	private final @Getter Map<Long, ISubscriptable<T>> mediumSubscriptables;
	private final @Getter Map<Long, ISubscriptable<T>> lowSubscriptables;

	public Subscriber()
	{
		this.highSubscriptables   = new HashMap<>();
		this.mediumSubscriptables = new HashMap<>();
		this.lowSubscriptables    = new HashMap<>();
	}

	private Map<Long, ISubscriptable<T>> get(Priority priorityIn)
	{
		switch (priorityIn)
		{
			case MEDIUM:
				return this.mediumSubscriptables;
			case LOW:
				return this.lowSubscriptables;
		}

		return this.highSubscriptables;
	}

	@Override
	public long subscribe(final ISubscriptable<T> subscriptableIn)
	{
		return this.subscribe(subscriptableIn, Priority.MEDIUM);
	}

	@Override
	public long subscribe(final ISubscriptable<T> subscriptableIn, final Priority priorityIn)
	{
		var map    = get(priorityIn);
		var handle = System.identityHashCode(subscriptableIn.get());
		if (map.containsKey(handle))
		{
			throw new RuntimeException(
					"[CRITICAL] Subscriber : unique handle is not anymore garanteed ! \"" + handle + "\" for \""
					+ subscriptableIn.get() + "\"[\"" + subscriptableIn + "\"] is already in use !");
		}

		map.put((long) handle, subscriptableIn);

		return handle;
	}

	@Override
	public ISubscriptable<T> subscribe(final T valueIn)
	{
		return this.subscribe(valueIn, Priority.MEDIUM);
	}

	@Override
	public ISubscriptable<T> subscribe(final T valueIn, final Priority priorityIn)
	{
		var map = get(priorityIn);

		var handle = System.identityHashCode(valueIn);
		if (map.containsKey(handle))
		{
			throw new RuntimeException(
					"[CRITICAL] Subscriber : unique handle is not anymore garanteed ! \"" + handle + "\" for \""
					+ valueIn + "\" is already in use !");
		}

		var subscriptable = new Subscriptable<>(valueIn, this, handle);
		map.put((long) handle, subscriptable);

		return subscriptable;
	}

	@Override
	public void unsubscribe(final long handleIn)
	{
		if (this.mediumSubscriptables.remove(handleIn) != null)
		{
			return;
		}

		if (this.lowSubscriptables.remove(handleIn) != null)
		{
			if (this.highSubscriptables.remove(handleIn) != null)
			{
				return;
			}
		}

		this.highSubscriptables.remove(handleIn);
	}

	@Override
	public boolean subscribed(final long handleIn)
	{
		return this.mediumSubscriptables.containsKey(handleIn) || this.lowSubscriptables.containsKey(handleIn)
		       || this.highSubscriptables.containsKey(handleIn);
	}

	@Override
	public Iterator<T> iterator()
	{
		return new SubscriberIterator(this.highSubscriptables, this.mediumSubscriptables, this.lowSubscriptables);
	}

	@Override
	public void forEach(final Consumer<? super T> actionIn)
	{
		this.highSubscriptables.values().forEach(subscriptable -> actionIn.accept(subscriptable.get()));
		this.mediumSubscriptables.values().forEach(subscriptable -> actionIn.accept(subscriptable.get()));
		this.lowSubscriptables.values().forEach(subscriptable -> actionIn.accept(subscriptable.get()));
	}

	public final class SubscriberIterator implements Iterator<T>
	{
		private final @Getter Map<Long, ISubscriptable<T>> highSubscriptables;
		private final @Getter Map<Long, ISubscriptable<T>> mediumSubscriptables;
		private final @Getter Map<Long, ISubscriptable<T>> lowSubscriptables;
		private               Iterator<ISubscriptable<T>>  iterator;
		private               int                          priority;

		public SubscriberIterator(final Map<Long, ISubscriptable<T>> highSubscriptablesIn,
		                          final Map<Long, ISubscriptable<T>> mediumSubscriptablesIn,
		                          Map<Long, ISubscriptable<T>> lowSubscriptablesIn)
		{
			this.highSubscriptables   = highSubscriptablesIn;
			this.mediumSubscriptables = mediumSubscriptablesIn;
			this.lowSubscriptables    = lowSubscriptablesIn;
			this.iterator             = highSubscriptablesIn.values().iterator();
			this.priority             = 0;
		}

		@Override
		public boolean hasNext()
		{
			var hasNext = false;
			while (!(hasNext = this.iterator.hasNext()))
			{
				if (!switchPriority())
				{
					return false;
				}
			}

			return hasNext;
		}

		public boolean switchPriority()
		{
			this.priority++;
			if (this.priority > 2)
			{
				return false;
			}

			switch (this.priority)
			{
				case 0:
					this.iterator = this.highSubscriptables.values().iterator();
					break;
				case 1:
					this.iterator = this.mediumSubscriptables.values().iterator();
					break;
				case 2:
					this.iterator = this.lowSubscriptables.values().iterator();
					break;
			}

			return true;
		}

		@Override
		public T next()
		{
			return this.iterator.next().get();
		}

		@Override
		public void remove()
		{
			this.iterator.remove();
		}

		@Override
		public void forEachRemaining(final Consumer<? super T> action)
		{
			this.iterator.forEachRemaining(subscriptable -> action.accept(subscriptable.get()));
		}
	}
}