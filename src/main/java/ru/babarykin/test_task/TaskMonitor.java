package ru.babarykin.test_task;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import static java.time.temporal.ChronoUnit.NANOS;

public class TaskMonitor implements Runnable {

    private final BlockingQueue<Task> monitorTaskQueue;
    private final BlockingQueue<Task> taskQueue;
    private final Lock lock;
    private final Condition notEmpty;

    public TaskMonitor(BlockingQueue<Task> monitorTaskQueue,
                       BlockingQueue<Task> taskQueue,
                       Lock lock,
                       Condition notEmpty) {
        this.monitorTaskQueue = monitorTaskQueue;
        this.taskQueue = taskQueue;
        this.lock = lock;
        this.notEmpty = notEmpty;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Task task = monitorTaskQueue.take();
                LocalDateTime now = LocalDateTime.now();
                if (task.shouldExecute(now)) {
                    taskQueue.offer(task);
                } else {
                    monitorTaskQueue.offer(task);
                    lock.lock();
                    notEmpty.awaitNanos(now.until(task.getStartTime(), NANOS));
                    lock.unlock();
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
