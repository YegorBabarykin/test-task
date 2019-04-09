package ru.babarykin.test_task;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.stream.IntStream;

public class TaskExecutor {

    private static final int INITIAL_QUEUE_CAPACITY = 100;

    private final BlockingQueue<Task> taskQueue;
    private final Thread[] threadPool;

    public TaskExecutor() {
        this(1);
    }

    public TaskExecutor(int poolSize) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be positive");
        }
        taskQueue = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, Task::compareTo);
        TaskConsumer taskConsumer = new TaskConsumer(taskQueue);
        threadPool = IntStream.range(0, poolSize)
                .mapToObj(i -> new Thread(taskConsumer))
                .peek(Thread::start)
                .toArray(Thread[]::new);
    }

    public <T> CompletableFuture<T> submit(Callable<T> callable, LocalDateTime startTime) {
        Task<T> task = new Task<>(startTime, callable);
        taskQueue.offer(task);
        return task.getCompletableFuture();
    }
}
