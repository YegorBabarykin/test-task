package ru.babarykin.test_task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.MILLIS;

public class App {
    public static void main(String[] args) throws InterruptedException {
        TaskExecutor taskExecutor = new TaskExecutor(10);
        LocalDateTime now = LocalDateTime.now();
        List<CompletableFuture<Integer>> features = IntStream.range(0, 1000)
                .mapToObj(i -> taskExecutor.submit(() -> {
                    System.out.println("executing " + i);
                    return i;
                }, now.plus(6 * i * i % 2 == 0 ? 1 : -1, MILLIS)))
                .collect(Collectors.toList());
        taskExecutor.submit(() -> null, now.plusSeconds(20));
        features.forEach(f -> {
            try {
                System.out.println(f.get());
            } catch (Exception e) {
            }
        });

//        taskExecutor.terminate();
    }
}
