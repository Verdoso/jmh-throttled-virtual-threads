package org.greeneyed.jmh_throttled_virtual_threads;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.greeneyed.jmh_throttled_virtual_threads.BenchmarkVirtualThreads.STRATEGY;
import org.greeneyed.jmh_throttled_virtual_threads.ConcurrencyMeter.Signaler;
import org.threeten.extra.AmountFormats;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VirtualThreadsTester {

	public static void main(String[] args) {
		if (args.length == 2) {
			log.info("Launching.. experiment");
			final STRATEGY strategy;
			try {
				strategy = STRATEGY.valueOf(args[0]);
			} catch (Exception e) {
				throw new IllegalArgumentException("Bad strategy specified: " + e.getMessage() + " It has to be one of "
						+ Stream.of(STRATEGY.values()).map(STRATEGY::name).collect(Collectors.joining(",")) + ".");
			}
			final double baseNumber;
			try {
				baseNumber = Double.parseDouble(args[1]);
			} catch (Exception e) {
				throw new IllegalArgumentException(
						"Bad base number specified. It has to be a double: " + e.getMessage());
			}
			Instant start = Instant.now();
			testExecution(strategy, baseNumber);
			Instant finish = Instant.now();
			log.info("Done in {}", AmountFormats.wordBased(Duration.between(start, finish), Locale.ENGLISH));
		} else {
			log.error("Strategy and base number are the only parameters required");
			log.error(
					"Usage: java --enable-preview -jar target/jmh-throttled-virtual-threads-0.0.1-SNAPSHOT.jar [VIRTUAL_THREADS|THROTTLED_VIRTUAL_THREADS] [baseNumber]");
		}
	}

	public static long testExecution(STRATEGY strategy, double baseNumber) {
		ConcurrencyMeter concurrencyMeter = new ConcurrencyMeter();
		try (var executor = strategy.getExecutorServiceGenerator().get()) {
			IntStream.range(0, 10_000).forEach(i -> {
				Runnable test = () -> {
					try (Signaler signaler = concurrencyMeter.getSignaler()) {
						log.debug("Started! {}", concurrencyMeter.maxAccomplishedConcurrency());
						double result = cpuConsumingMethod(baseNumber);
						// Printing debug so the compiler does not decide it is useless and removes it
						log.debug("Result: {}", result);
						log.debug("Finished {}! {}", i, concurrencyMeter.currentConcurrency());
					}
				};
				executor.submit(test);
			});
		}
		log.info("{}: {} max concurrency ", strategy, concurrencyMeter.maxAccomplishedConcurrency());
		return concurrencyMeter.maxAccomplishedConcurrency();
	}

	private static double cpuConsumingMethod(double baseNumber) {
		double result = 0;
		for (double x = Math.pow(baseNumber, 7); x >= 0; x--) {
			result += Math.atan(x) * Math.tan(x);
		}
// This causes a massive effect if throttled and with a small factor		
// Some imaginary delay (I/O?) that causes the thread to pause
//		try {
//			Thread.sleep(Duration.ofMillis(50));
//		} catch (InterruptedException e) {
//			// Intrrupted? Why? :D
//		}
		return result;
	}
}
