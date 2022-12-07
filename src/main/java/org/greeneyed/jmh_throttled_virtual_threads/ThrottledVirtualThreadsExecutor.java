package org.greeneyed.jmh_throttled_virtual_threads;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ThrottledVirtualThreadsExecutor implements ExecutorService {
	private final Semaphore semaphore;
	private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

	private class ThrottledRunnable implements Runnable {

		private final Semaphore semaphore;
		private final Runnable runnable;
		private boolean interrupted;

		ThrottledRunnable(Semaphore semaphore, Runnable runnable) {
			this.semaphore = semaphore;
			this.runnable = runnable;
		}

		Runnable getRunnable() {
			return runnable;
		}

		void interrupt() {
			this.interrupted = true;
		}

		@Override
		public void run() {
			try {
				this.semaphore.acquire();
				try {
					if (!interrupted) {
						runnable.run();
					}
				} finally {
					this.semaphore.release();
				}
			} catch (InterruptedException e) {
				// Thread.currentThread().interrupt(); With virtual threads?
				// This could happen if we signal all tasks waiting on the semaphore to stop,
				// for example due to a shutdownNow command
			}
		}
	}

	private class ThrottledCallable<T> implements Callable<T> {

		private final Semaphore semaphore;
		private final Callable<T> callable;

		ThrottledCallable(Semaphore semaphore, Callable<T> callable) {
			this.semaphore = semaphore;
			this.callable = callable;
		}

		@Override
		public T call() throws Exception {
			try {
				this.semaphore.acquire();
				try {
					return this.callable.call();
				} finally {
					this.semaphore.release();
				}
			} catch (InterruptedException e) {
				throw e;
			}
		}
	}

	// private boolean shutdown = false;

	public ThrottledVirtualThreadsExecutor(int concurrencyLevel, boolean fair) {
		this.semaphore = new Semaphore(concurrencyLevel, fair);
	}

	public ThrottledVirtualThreadsExecutor(int concurrencyLevel) {
		this(concurrencyLevel, false);
	}

	@Override
	public void close() {
		this.executor.close();
	}

	@Override
	public void execute(Runnable command) {
		this.executor.execute(new ThrottledRunnable(this.semaphore, command));
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.executor.submit(new ThrottledCallable<>(this.semaphore, task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return this.executor.submit(new ThrottledRunnable(this.semaphore, task), result);
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return this.executor
				.invokeAll(tasks.stream().map(task -> new ThrottledCallable<>(this.semaphore, task)).toList());
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return this.executor.invokeAll(
				tasks.stream().map(task -> new ThrottledCallable<>(this.semaphore, task)).toList(), timeout, unit);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return this.executor.submit(new ThrottledRunnable(this.semaphore, task));
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
			throws InterruptedException, ExecutionException {
		return this.executor
				.invokeAny(tasks.stream().map(task -> new ThrottledCallable<>(this.semaphore, task)).toList());
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return this.executor.invokeAny(
				tasks.stream().map(task -> new ThrottledCallable<>(this.semaphore, task)).toList(), timeout, unit);
	}

	@Override
	public void shutdown() {
		this.executor.shutdown();
	}

	@Override
	public List<Runnable> shutdownNow() {
		return this.executor.shutdownNow().stream().map(runnable -> {
			if (runnable instanceof ThrottledRunnable throttledRunnable) {
				throttledRunnable.interrupt();
				return throttledRunnable.getRunnable();
			} else {
				return runnable;
			}
		}).toList();
	}

	@Override
	public boolean isShutdown() {
		return this.executor.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return this.executor.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return this.executor.awaitTermination(timeout, unit);
	}
}