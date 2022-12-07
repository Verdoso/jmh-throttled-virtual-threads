package org.greeneyed.jmh_throttled_virtual_threads;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

public class BenchmarkVirtualThreads {

	@RequiredArgsConstructor
	@Getter
	enum STRATEGY {
		VIRTUAL_THREADS(() -> Executors.newVirtualThreadPerTaskExecutor()),
		THROTTLED_VIRTUAL_THREADS(() -> new ThrottledVirtualThreadsExecutor(30, true));

		private final Supplier<ExecutorService> executorServiceGenerator;
	}
	
    private static final int SMALL_FACTOR = 2;
    
    private static final int LONG_FACTOR = 5;
	
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public long testFreeVirtualThreadsWithSmallFactor() {
        return VirtualThreadsTester.testExecution(STRATEGY.VIRTUAL_THREADS,SMALL_FACTOR);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public long testFreeVirtualThreadsWithLongFactor() {
    	return VirtualThreadsTester.testExecution(STRATEGY.VIRTUAL_THREADS,LONG_FACTOR);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public long testThrottledVirtualThreadsWithSmallFactor() {
    	return VirtualThreadsTester.testExecution(STRATEGY.THROTTLED_VIRTUAL_THREADS,SMALL_FACTOR);
    }
    
    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    public long testThrottledVirtualThreadsWithLongFactor() {
    	return VirtualThreadsTester.testExecution(STRATEGY.THROTTLED_VIRTUAL_THREADS,LONG_FACTOR);
    }
    
}
