
# Virtual Threads Microbenchmark
This is a very simple project to experiment with the "[Virtual Threads](https://www.infoq.com/articles/java-virtual-threads/)" feature introduced in Java 19.
The idea is to see how we can control concurrency when using Virtual Threads, given that right now there is no equivalent to the fixed-size pool of native threads that can be created using `Executors.newFixedThreadPool`. This set of classes constitute a [JMH](https://github.com/openjdk/jmh) benchmark that can be used to compare different strategies.

## Main classes
*  **[ThrottledVirtualThreadsExecutor](https://github.com/Verdoso/jmh-throttled-virtual-threads/blob/master/src/main/java/org/greeneyed/jmh_throttled_virtual_threads/ThrottledVirtualThreadsExecutor.java)** is the ExecutorService implementation, that might be used instead of `java.util.concurrent.ThreadPoolExecutor`, that uses Virtual Threads to execute the tasks submitted but limits concurrency of thhe submmitted tasks to the limit specified when creating the instance.
*  **[VirtualThreadsTester](https://github.com/Verdoso/jmh-throttled-virtual-threads/blob/master/src/main/java/org/greeneyed/jmh_throttled_virtual_threads/VirtualThreadsTester.java)** is the class that performs the experiment. In particular, its `cpuConsumingMethod()` is the one that the tasks execute concurrently to see the differences. This class can perform the experiment using two strategies, using the aforementioned `ThrottledVirtualThreadsExecutor` or a simple `Executors.newVirtualThreadPerTaskExecutor()`.
*  **[BenchmarkVirtualThreads](https://github.com/Verdoso/jmh-throttled-virtual-threads/blob/master/src/main/java/org/greeneyed/jmh_throttled_virtual_threads/BenchmarkVirtualThreads.java)** is the class that launches the experiment using the [JMH benchmarking framework.](https://github.com/openjdk/jmh)
*  **[ConcurrencyMeter](https://github.com/Verdoso/jmh-throttled-virtual-threads/blob/master/src/main/java/org/greeneyed/jmh_throttled_virtual_threads/ConcurrencyMeter.java)** is an auxiliary class that is used to measure the maximum level of concurrency achieved during the experiments.

## Building the bechmark
In order to build and execute this project, you need to have Java 19 installed. Execute `mvn -version` and verify that you have Java 19 ready to be used from Maven and then simply execute `mvn install`.

## Running the bechmark
Again, make sure you have Java 19 installed and ready by executing `java -version` and once you have verified you are using Java 19, you can execute the benchmark with `java --enable-preview -jar target/vt-benchmarks.jar`. Be aware that the ful execution of the becnhmark can take quite a while (>30m depending on your CPU).

## Running individual tests
You can run some a quick test launching directly the class that creates the threads and performs the tasks. However, be adviced that running the test once suffers from the typical microbenchmarking issues (JIT compilation interference, more sensible to the external conditions...) so take the results with a BIG grain of salt.

In order to run one execution, Maven needs to have Java 19 available and then you can issue the following command:
`mvn exec:java -Dexec.args="THROTTLED_VIRTUAL_THREADS 2"`
where the first parameter is the strategy (THROTTLED_VIRTUAL_THREADS or VIRTUAL_THREADS) and the second one is the base number for the CPU consuming method (the higher, the more CPU it consumes).

## Experimenting
If you want to test different situations, you can change the level of concurrency for the THROTTLED_VIRTUAL_THREADS strategy (@BenchmarkVirtualThreads class, THROTTLED_VIRTUAL_THREADS enum definition); You can also change the `cpuConsumingMethod` method implementation (@VirtualThreadsTester class) (p.e. see how adding a small sleep time affects the differences). Or you can change completely the implementation and make it call a DB or a external web service and see which strategy or concurrency level works best for you.

Have fun!