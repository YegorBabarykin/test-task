package ru.babarykin.test_task;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

public class Task<T> implements Comparable<Task> {

    private static final AtomicLong idGenerator = new AtomicLong(0);


    private final Long id;
    private final LocalDateTime startTime;
    private final Callable<T> callable;
    private final CompletableFuture<T> completableFuture;

    public Task(LocalDateTime startTime, Callable<T> callable) {
        if (startTime == null || callable == null) {
            throw new NullPointerException();
        }
        this.id = idGenerator.incrementAndGet();
        this.startTime = startTime;
        this.callable = callable;
        this.completableFuture = new CompletableFuture<>();
    }

    public Callable<T> getCallable() {
        return callable;
    }

    public CompletableFuture<T> getCompletableFuture() {
        return completableFuture;
    }

    @Override
    public int compareTo(Task o) {
        int compareByTime = this.startTime.compareTo(o.startTime);
        return compareByTime == 0 ? this.id.compareTo(o.id) : compareByTime;
    }

    public boolean shouldExecuteNow() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(startTime) || now.isEqual(startTime);
    }

}
