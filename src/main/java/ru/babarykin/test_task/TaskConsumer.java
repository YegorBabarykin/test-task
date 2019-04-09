package ru.babarykin.test_task;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaskConsumer implements Runnable {

    private static final Lock lock = new ReentrantLock();

    private final BlockingQueue<Task> queue;

    public TaskConsumer(BlockingQueue<Task> queue) {
        this.queue = queue;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        try {
            while (true) {
                Task task = getTask();
                if (task != null && !task.getCompletableFuture().isCancelled()) {
                    try {
                        Object result = task.getCallable().call();
                        task.getCompletableFuture().complete(result);
                    } catch (Throwable e) {
                        task.getCompletableFuture().completeExceptionally(e);
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            //TODO logging
        }
    }

    private Task getTask() throws InterruptedException {
        Task task = queue.peek();
        LocalDateTime now = LocalDateTime.now();
        if (task != null && task.shouldExecute(now)) {
            lock.lock();
            try {
                task = queue.peek();
                if (task != null && task.shouldExecute(now)) {
                    return queue.take();
                }
            } finally {
                lock.unlock();
            }
        }
        return null;
    }
}