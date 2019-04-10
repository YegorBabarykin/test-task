package ru.babarykin.test_task;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class TaskExecutor {

    private static final int INITIAL_QUEUE_CAPACITY = 100;

    private final AtomicLong idGenerator = new AtomicLong(0);

    private final BlockingQueue<Task> monitorTaskQueue;
    private final BlockingQueue<Task> taskQueue;
    private final Thread[] threadPool;
    private final Thread taskMonitorThread;
    private final Lock lock;
    private final Condition notEmpty;

    public TaskExecutor() {
        this(1);
    }

    public TaskExecutor(int poolSize) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("poolSize must be positive");
        }
        taskQueue = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, Task::compareTo);
        monitorTaskQueue = new PriorityBlockingQueue<>(INITIAL_QUEUE_CAPACITY, Task::compareTo);
        TaskConsumer taskConsumer = new TaskConsumer(taskQueue);
        threadPool = IntStream.range(0, poolSize)
                .mapToObj(i -> new Thread(taskConsumer))
                .peek(Thread::start)
                .toArray(Thread[]::new);
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        taskMonitorThread = new Thread(new TaskMonitor(monitorTaskQueue, taskQueue, lock, notEmpty));
        taskMonitorThread.start();
    }

    public <T> CompletableFuture<T> submit(Callable<T> callable, LocalDateTime startTime) {
        Task<T> task = new Task<>(idGenerator.incrementAndGet(), startTime, callable);
        monitorTaskQueue.offer(task);
        lock.lock();
        notEmpty.signal();
        lock.unlock();
        return task.getCompletableFuture();
    }

    public void terminate() {
        for (Thread thread : threadPool) {
            thread.interrupt();
        }
        taskMonitorThread.interrupt();
    }
}
