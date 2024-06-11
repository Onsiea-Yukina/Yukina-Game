package fr.yukina.game.utils.thread;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

public class OrderingMachine<K, V, R>
{
	private final IIFunction<Queue<V>> orderingProcess;
	private final IOIFunction<R, V>    executorProcess;
	private final IOIFunction<K, V>    identifier;

	private final @Getter Queue<V>              waitingQueue;
	private final         Queue<R>              processedQueue;
	private final         Map<K, Processing<V>> processMap;

	private final List<IIFunction<V>> orderedListeners;
	private final List<IIFunction<V>> processListener;
	private final List<IIFunction<R>> processedListeners;

	private final Thread          orderingThread;
	private final ExecutorService executor;

	private @Setter @Getter volatile long orderingInterval;
	private @Setter @Getter volatile int  maxSubmitCount;

	private @Getter volatile boolean isRunning;
	private @Getter volatile boolean isStopped;

	public OrderingMachine(IIFunction<Queue<V>> orderingProcess, IOIFunction<R, V> processIn,
	                       IOIFunction<K, V> identifierIn, int executorThreadCountIn, Queue<V> queueIn)
	{
		this.orderingProcess = orderingProcess;
		this.executorProcess = processIn;
		this.identifier      = identifierIn;

		this.waitingQueue   = queueIn;
		this.processMap     = new ConcurrentHashMap<>();
		this.processedQueue = new ConcurrentLinkedQueue<>();

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

					                                 var       processing = new Processing<>(value);
					                                 final var key        = this.identifier.process(value);
					                                 Runnable runnable = () ->
					                                 {
						                                 try
						                                 {
							                                 for (var listener : this.processListener)
							                                 {
								                                 listener.process(value);
							                                 }

							                                 var result = this.executorProcess.process(value);

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
					                                 processing.set(this.executor.submit(runnable));
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
		for (var processing : this.processMap.values())
		{
			var task = processing.task();
			if (task != null)
			{
				task.cancel(true);
			}
		}
		this.processMap.clear();
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

	public interface IIFunction<I>
	{
		void process(I inputIn);
	}

	public interface IOIFunction<O, I>
	{
		O process(I inputIn);
	}

	@Getter
	private final class Processing<V>
	{
		private V         value;
		private Future<?> task;

		public Processing(V valueIn)
		{
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
}