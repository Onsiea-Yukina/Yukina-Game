package fr.yukina.game.utils.thread;

import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.*;

public class OrderingMachine<K, V, R>
{
	private final IIFunction<Queue<V>> orderingProcess;
	private final IOIFunction<R, V>    executorProcess;
	private final IOIFunction<K, V>    identifier;

	private final @Getter ConcurrentPriorityQueue<K, V> waitingQueue;
	private final         Queue<R>                      processedQueue;
	private final         Map<K, Processing<K, V>>      processMap;
	private final         Map<K, State>                 uniqueMap;

	private final List<IIFunction<V>> orderedListeners;
	private final List<IIFunction<V>> processListener;
	private final List<IIFunction<R>> processedListeners;

	private final Thread          orderingThread;
	private final ExecutorService executor;

	private @Setter volatile long orderingInterval;
	private @Setter volatile int  maxSubmitCount;

	private @Getter volatile boolean isRunning;
	private @Getter volatile boolean isStopped;

	public OrderingMachine(IIFunction<Queue<V>> orderingProcess, IOIFunction<R, V> processIn,
	                       IOIFunction<K, V> identifierIn, int executorThreadCountIn, Comparator<V> comparator)
	{
		this.orderingProcess = orderingProcess;
		this.executorProcess = processIn;
		this.identifier      = identifierIn;

		this.waitingQueue   = new ConcurrentPriorityQueue<>(comparator, identifierIn);
		this.processMap     = new ConcurrentHashMap<>();
		this.processedQueue = new ConcurrentLinkedQueue<>();
		this.uniqueMap      = new ConcurrentHashMap<>();

		this.orderedListeners   = new ArrayList<>();
		this.processListener    = new ArrayList<>();
		this.processedListeners = new ArrayList<>();

		this.executor       = Executors.newFixedThreadPool(executorThreadCountIn);
		this.orderingThread = new Thread(() ->
		                                 {
			                                 while (this.isRunning)
			                                 {
				                                 this.orderingProcess.process(this.waitingQueue);

				                                 while (!this.waitingQueue.isEmpty() && this.isRunning)
				                                 {
					                                 if (this.maxSubmitCount > 0
					                                     && this.processMap.size() >= this.maxSubmitCount)
					                                 {
						                                 break;
					                                 }

					                                 var value = this.waitingQueue.poll();
					                                 for (var listener : this.orderedListeners)
					                                 {
						                                 listener.process(value);
					                                 }

					                                 var key        = this.identifier.process(value);
					                                 var processing = new Processing<>(key, value);
					                                 this.processMap.put(key, processing);
					                                 this.uniqueMap.replace(key, State.PROCESSING);
					                                 Runnable runnable = () ->
					                                 {
						                                 try
						                                 {
							                                 for (var listener : this.processListener)
							                                 {
								                                 listener.process(value);
							                                 }

							                                 var result = this.executorProcess.process(value);

							                                 synchronized (this.uniqueMap)
							                                 {
								                                 this.uniqueMap.replace(key, State.PROCESSED);
							                                 }

							                                 synchronized (this.processedQueue)
							                                 {
								                                 this.processedQueue.add(result);
							                                 }

							                                 for (var listener : this.processedListeners)
							                                 {
								                                 listener.process(result);
							                                 }
						                                 }
						                                 catch (Exception e)
						                                 {
							                                 e.printStackTrace();
							                                 Thread.currentThread().interrupt();
						                                 }
						                                 finally
						                                 {
							                                 if (key != null)
							                                 {
								                                 synchronized (this.processMap)
								                                 {
									                                 this.processMap.remove(key);
								                                 }
							                                 }
						                                 }
					                                 };
					                                 try
					                                 {
						                                 processing.set(this.executor.submit(runnable));
					                                 }
					                                 catch (RejectedExecutionException e)
					                                 {

					                                 }
				                                 }

				                                 if (this.orderingInterval > 0L && this.isRunning)
				                                 {
					                                 try
					                                 {
						                                 Thread.sleep(this.orderingInterval);
					                                 }
					                                 catch (InterruptedException eIn)
					                                 {
						                                 eIn.printStackTrace();
					                                 }
				                                 }
			                                 }

			                                 this.isStopped = true;
		                                 });
	}

	public final OrderingMachine<K, V, R> listenOrder(IIFunction<V> listenerIn)
	{
		this.orderedListeners.add(listenerIn);
		return this;
	}

	public final OrderingMachine<K, V, R> listenProcess(IIFunction<V> listenerIn)
	{
		this.processListener.add(listenerIn);
		return this;
	}

	public final OrderingMachine<K, V, R> listenProcessed(IIFunction<R> listenerIn)
	{
		this.processedListeners.add(listenerIn);
		return this;
	}

	public final void start()
	{
		this.isRunning = true;
		this.isStopped = false;
		this.orderingThread.start();
	}

	public final void stopTasks()
	{
		this.waitingQueue.clear();
		/*for (var processing : this.processMap.values())
		{
			var state = this.uniqueMap.get(processing.key());
			if (state == State.PROCESSED)
			{
				continue;
			}
			var task = processing.task();
			if (task != null)
			{
				if (task.cancel(true) && task.isCancelled())
				{
					synchronized (this.uniqueMap)
					{
						this.uniqueMap.remove(processing.key());
					}
				}
			}
		}
		this.processMap.clear();*/
	}

	public final void stop()
	{
		this.isRunning = false;
		this.stopTasks();
		try
		{
			this.orderingThread.join();
			this.executor.shutdown();
			this.executor.awaitTermination(2, TimeUnit.SECONDS);
			this.executor.shutdownNow();

			this.orderingThread.interrupt();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		this.isStopped = true;

		this.processMap.clear();
		this.processedQueue.clear();
	}

	public final void submit(V valueIn)
	{
		this.submit(this.identifier.process(valueIn), valueIn);
	}

	public final void submit(K keyIn, V valueIn)
	{
		if (this.alreadyExists(keyIn))
		{
			return;
		}
		this.waitingQueue.add(valueIn);
		synchronized (this.uniqueMap)
		{
			this.uniqueMap.put(keyIn, State.WAITING);
		}
	}

	public final void remove(K keyIn)
	{
		synchronized (this.uniqueMap)
		{
			this.waitingQueue.remove(keyIn);
			this.uniqueMap.remove(keyIn);
			this.processMap.remove(keyIn);
		}
	}

	public final boolean alreadyExists(K keyIn)
	{
		return this.uniqueMap.containsKey(keyIn);
	}

	@Getter
	private final class Processing<K, V>
	{
		private K         key;
		private V         value;
		private Future<?> task;

		public Processing(K keyIn, V valueIn)
		{
			this.key   = keyIn;
			this.value = valueIn;
		}

		public void set(Future<?> taskIn)
		{
			this.task = taskIn;
		}

		public void cancel()
		{
			if (this.task != null)
			{
				this.task.cancel(true);
			}
		}
	}

	public enum State
	{
		WAITING, PROCESSING, PROCESSED;
	}
}