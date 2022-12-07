package org.greeneyed.jmh_throttled_virtual_threads;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrencyMeter {
	private long currentConcurrency = 0;
	private long maxConcurrency = 0;
	private Lock updaterLock = new ReentrantLock();
	
	public long maxAccomplishedConcurrency() {
		return maxConcurrency;
	}
	
	public long currentConcurrency() {
		return currentConcurrency;
	}
	
	public Signaler getSignaler() {
		return new Signaler();
	}

	public class Signaler implements AutoCloseable {

		Signaler() {
			updaterLock.lock();
			try {
				currentConcurrency +=1;
				// If not using a lock, some other threads might decrement currentConcurrency here, losing this update
				maxConcurrency = Math.max(maxConcurrency, currentConcurrency);
			} finally {
				updaterLock.unlock();
			}
		}

		@Override
		public void close() {
			updaterLock.lock();
			try {
				currentConcurrency -=1;
			} finally {
				updaterLock.unlock();
			}
		}
	}
}