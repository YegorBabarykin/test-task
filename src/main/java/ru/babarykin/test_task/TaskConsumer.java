package ru.babarykin.test_task;

import java.util.concurrent.BlockingQueue;

public class TaskConsumer implements Runnable {

    private final BlockingQueue<Task> queue;

    public TaskConsumer(BlockingQueue<Task> queue) {
        this.queue = queue;
    }

    @SuppressWarnings("unchecked")
    public void run() {
        while (true) {
            try {
                Task task = queue.take();
                if (!task.getCompletableFuture().isCancelled()) {
                    try {
                        Object result = task.getCallable().call();
                        task.getCompletableFuture().complete(result);
                    } catch (Throwable e) {
                        task.getCompletableFuture().completeExceptionally(e);
                        throw e;
                    }
                }
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
            }
        }
    }

}