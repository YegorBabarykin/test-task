package ru.babarykin.test_task;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static java.time.temporal.ChronoUnit.NANOS;

public class TaskConsumer implements Runnable {

    private final BlockingQueue<Task> queue;
    private final Lock lock;
    private final Condition notEmpty;

    public TaskConsumer(BlockingQueue<Task> queue,
                        Lock lock,
                        Condition notEmpty) {
        this.queue = queue;
        this.lock = lock;
        this.notEmpty = notEmpty;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        while (true) {
            try {
                Task task = queue.take();
                LocalDateTime now = LocalDateTime.now();
                if (task.shouldExecute(now)) {
                    if (!task.getCompletableFuture().isCancelled()) {
                        try {
                            Object result = task.getCallable().call();
                            task.getCompletableFuture().complete(result);
                        } catch (Throwable e) {
                            task.getCompletableFuture().completeExceptionally(e);
                            throw e;
                        }
                    }
                } else {
                    //return task to queue
                    queue.offer(task);
                    //to get the correct state of monitor
                    lock.lock();
                    //async wait notification from taskExecutor
                    notEmpty.awaitNanos(now.until(task.getStartTime(), NANOS));
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
            }
        }
    }

}