package ru.babarykin.test_task;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

public class TaskTest {

    @Test
    public void testCompareByDateTime() {
        Task task1 = new Task<>(1, LocalDateTime.now(), () -> null);
        Task task2 = new Task<>(2, LocalDateTime.now().plusDays(1), () -> null);
        Task task3 = new Task<>(3, LocalDateTime.now().plusNanos(1), () -> null);
        Assert.assertTrue(task1.compareTo(task2) < 0);
        Assert.assertTrue(task1.compareTo(task3) < 0);
        Assert.assertTrue(task2.compareTo(task3) > 0);
    }

    @Test
    public void testCompareByTaskId() {
        LocalDateTime sameDateTime = LocalDateTime.now();
        Task task1 = new Task<>(1, sameDateTime, () -> null);
        Task task2 = new Task<>(2, sameDateTime, () -> null);
        Task task3 = new Task<>(3, sameDateTime, () -> null);
        Assert.assertTrue(task1.compareTo(task2) < 0);
        Assert.assertTrue(task1.compareTo(task3) < 0);
        Assert.assertTrue(task2.compareTo(task3) < 0);
    }

    @Test
    public void testShouldExecute() {
        LocalDateTime dateTime = LocalDateTime.now();
        Task task = new Task<>(1, dateTime, () -> null);
        Assert.assertTrue(task.shouldExecute(dateTime));
        Assert.assertTrue(task.shouldExecute(dateTime.plusNanos(1)));
        Assert.assertFalse(task.shouldExecute(dateTime.minusNanos(1)));
    }

}
