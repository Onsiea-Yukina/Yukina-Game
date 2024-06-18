package fr.yukina.game.utils.thread;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class ConcurrentPriorityQueue<K, V> implements Queue<V>
{
	private final ConcurrentHashMap<K, V>  map;
	private final PriorityBlockingQueue<V> queue;
	private final Comparator<V>            comparator;
	private final IOIFunction<K, V>        keyExtractor;

	public ConcurrentPriorityQueue(Comparator<V> comparator, IOIFunction<K, V> keyExtractor)
	{
		this.map          = new ConcurrentHashMap<>();
		this.queue        = new PriorityBlockingQueue<>(11, comparator);
		this.comparator   = comparator;
		this.keyExtractor = keyExtractor;
	}

	@Override
	public boolean add(V value)
	{
		K key = keyExtractor.process(value);
		synchronized (map)
		{
			if (map.containsKey(key))
			{
				return false;
			}
			map.put(key, value);
			queue.offer(value);
			return true;
		}
	}

	@Override
	public boolean offer(V value)
	{
		return add(value);
	}

	@Override
	public V poll()
	{
		synchronized (map)
		{
			V value = queue.poll();
			if (value != null)
			{
				K key = keyExtractor.process(value);
				map.remove(key);
			}
			return value;
		}
	}

	@Override
	public V remove()
	{
		return poll();
	}

	@Override
	public V peek()
	{
		return queue.peek();
	}

	@Override
	public V element()
	{
		return queue.element();
	}

	public boolean containsKey(K keyIn)
	{
		return this.map.containsKey(keyIn);
	}

	@Override
	public boolean contains(Object o)
	{
		if (o == null)
		{
			return false;
		}
		return map.containsKey(keyExtractor.process((V) o));
	}

	@Override
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}

	@Override
	public int size()
	{
		return queue.size();
	}

	@Override
	public void clear()
	{
		synchronized (map)
		{
			map.clear();
			queue.clear();
		}
	}

	@Override
	public Iterator<V> iterator()
	{
		return queue.iterator();
	}

	@Override
	public Object[] toArray()
	{
		return queue.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a)
	{
		return queue.toArray(a);
	}

	@Override
	public boolean remove(Object o)
	{
		if (o == null)
		{
			return false;
		}

		try
		{
			var key = (K) o;
			synchronized (map)
			{
				var value = map.remove(key);
				if (value != null)
				{
					return queue.remove(value);
				}
				return false;
			}
		}
		catch (ClassCastException e)
		{
		}

		V value = (V) o;
		K key   = keyExtractor.process(value);
		synchronized (map)
		{
			if (map.remove(key) != null)
			{
				return queue.remove(value);
			}
			return false;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c)
	{
		for (Object e : c)
		{
			if (!contains(e))
			{
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends V> c)
	{
		boolean modified = false;
		for (V e : c)
		{
			if (add(e))
			{
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean modified = false;
		for (Object e : c)
		{
			if (remove(e))
			{
				modified = true;
			}
		}
		return modified;
	}

	@Override
	public boolean retainAll(Collection<?> c)
	{
		boolean     modified = false;
		Iterator<V> it       = iterator();
		while (it.hasNext())
		{
			V e = it.next();
			if (!c.contains(e))
			{
				it.remove();
				map.remove(keyExtractor.process(e));
				modified = true;
			}
		}
		return modified;
	}
}