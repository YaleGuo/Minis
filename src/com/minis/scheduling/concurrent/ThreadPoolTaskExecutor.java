package com.minis.scheduling.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.minis.util.concurrent.ListenableFuture;
import com.minis.util.concurrent.ListenableFutureTask;

public class ThreadPoolTaskExecutor{
	private final Object poolSizeMonitor = new Object();
	
	private int corePoolSize = 1;
	private int maxPoolSize = Integer.MAX_VALUE;
	private int keepAliveSeconds = 60;
	private int queueCapacity = Integer.MAX_VALUE;
	private RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.DiscardPolicy();
	
	private ThreadPoolExecutor threadPoolExecutor;

	public void setCorePoolSize(int corePoolSize) {
		synchronized (this.poolSizeMonitor) {
			this.corePoolSize = corePoolSize;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setCorePoolSize(corePoolSize);
			}
		}
	}

	public int getCorePoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.corePoolSize;
		}
	}

	public void setMaxPoolSize(int maxPoolSize) {
		synchronized (this.poolSizeMonitor) {
			this.maxPoolSize = maxPoolSize;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setMaximumPoolSize(maxPoolSize);
			}
		}
	}

	public int getMaxPoolSize() {
		synchronized (this.poolSizeMonitor) {
			return this.maxPoolSize;
		}
	}

	public void setKeepAliveSeconds(int keepAliveSeconds) {
		synchronized (this.poolSizeMonitor) {
			this.keepAliveSeconds = keepAliveSeconds;
			if (this.threadPoolExecutor != null) {
				this.threadPoolExecutor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
			}
		}
	}

	public int getKeepAliveSeconds() {
		synchronized (this.poolSizeMonitor) {
			return this.keepAliveSeconds;
		}
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public ExecutorService initializeExecutor() {
		BlockingQueue<Runnable> queue = createQueue(this.queueCapacity);

		ThreadPoolExecutor executor;
		executor = new ThreadPoolExecutor(
			this.corePoolSize, this.maxPoolSize, this.keepAliveSeconds, TimeUnit.SECONDS,
			queue,Executors.defaultThreadFactory(),this.rejectedExecutionHandler);

		this.threadPoolExecutor = executor;
		return executor;
	}

	protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
		if (queueCapacity > 0) {
			return new LinkedBlockingQueue<>(queueCapacity);
		}
		else {
			return new SynchronousQueue<>();
		}
	}

	public ThreadPoolExecutor getThreadPoolExecutor() throws IllegalStateException {
		return this.threadPoolExecutor;
	}

	public int getPoolSize() {
		if (this.threadPoolExecutor == null) {
			return this.corePoolSize;
		}
		return this.threadPoolExecutor.getPoolSize();
	}

	public int getActiveCount() {
		if (this.threadPoolExecutor == null) {
			return 0;
		}
		return this.threadPoolExecutor.getActiveCount();
	}


	public void execute(Runnable task) {
		Executor executor = getThreadPoolExecutor();
		try {
			executor.execute(task);
		}
		catch (RejectedExecutionException ex) {
			throw ex;
		}
	}

	public void execute(Runnable task, long startTimeout) {
		execute(task);
	}

	public Future<?> submit(Runnable task) {
		ExecutorService executor = getThreadPoolExecutor();
		try {
			return executor.submit(task);
		}
		catch (RejectedExecutionException ex) {
			throw ex;
		}
	}

	public <T> Future<T> submit(Callable<T> task) {
		ExecutorService executor = getThreadPoolExecutor();
		try {
			return executor.submit(task);
		}
		catch (RejectedExecutionException ex) {
			throw ex;
		}
	}

	public RejectedExecutionHandler getRejectedExecutionHandler() {
		return rejectedExecutionHandler;
	}

	public void setRejectedExecutionHandler(RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler = rejectedExecutionHandler;
	}

	public ListenableFuture<?> submitListenable(Runnable task) {
		ExecutorService executor = getThreadPoolExecutor();
		try {
			ListenableFutureTask<Object> future = new ListenableFutureTask<>(task, null);
			executor.execute(future);
			return future;
		}
		catch (RejectedExecutionException ex) {
			throw ex;
		}
	}

	public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
		ExecutorService executor = getThreadPoolExecutor();
		try {
			ListenableFutureTask<T> future = new ListenableFutureTask<>(task);
			executor.execute(future);
			return future;
		}
		catch (RejectedExecutionException ex) {
			throw ex;
		}
	}

}
