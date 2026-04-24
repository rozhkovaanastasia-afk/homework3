import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class Main {

    static final int LIMIT = 10_000_000;
    static final int THREADS = Runtime.getRuntime().availableProcessors();

    public static void main(String[] args) {

        System.out.println("Collatz calculation started");
        System.out.println("Numbers: from 1 to " + LIMIT);
        System.out.println("Threads: " + THREADS);

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        AtomicLong totalSteps = new AtomicLong(0);

        long startTime = System.nanoTime();

        int blockSize = LIMIT / THREADS;

        for (int t = 0; t < THREADS; t++) {

            final int start = t * blockSize + 1;
            final int end = (t == THREADS - 1) ? LIMIT : (t + 1) * blockSize;

            executor.submit(() -> {
                long localSum = 0;

                for (int number = start; number <= end; number++) {
                    localSum += collatzSteps(number);
                }

                totalSteps.addAndGet(localSum);
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();

        double executionTimeSec = (endTime - startTime) / 1_000_000_000.0;
        double averageSteps = (double) totalSteps.get() / LIMIT;

        System.out.println("Calculation finished");
        System.out.println("Total steps: " + totalSteps.get());
        System.out.println("Average steps: " + averageSteps);
        System.out.println("Execution time, sec: " + executionTimeSec);

        saveResultsToCsv(totalSteps.get(), averageSteps, executionTimeSec);
    }

    static long collatzSteps(long n) {
        long steps = 0;

        while (n != 1) {
            if (n % 2 == 0) {
                n = n / 2;
            } else {
                n = 3 * n + 1;
            }

            steps++;
        }

        return steps;
    }

    static void saveResultsToCsv(long totalSteps, double averageSteps, double executionTimeSec) {
        new File("results").mkdirs();

        String filePath = "results/collatz_parallel_results.csv";

        try (FileWriter writer = new FileWriter(filePath)) {

            writer.append("N,threads,total_steps,average_steps,execution_time_sec\n");

            writer.append(
                    LIMIT + "," +
                            THREADS + "," +
                            totalSteps + "," +
                            averageSteps + "," +
                            executionTimeSec + "\n"
            );

            System.out.println("Results saved to " + filePath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
