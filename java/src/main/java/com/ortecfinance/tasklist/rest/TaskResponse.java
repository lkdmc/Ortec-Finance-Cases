package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.Task;

import java.time.format.DateTimeFormatter;

/**
 * API representation of a task. The deadline is formatted as dd-MM-yyyy to match
 * the rest of the application, and is {@code null} when the task has no deadline.
 */
public record TaskResponse(long id, String description, boolean done, String deadline) {
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static TaskResponse from(Task task) {
        String deadline = task.getDeadline() == null
            ? null
            : task.getDeadline().format(DEADLINE_FORMAT);
        return new TaskResponse(task.getId(), task.getDescription(), task.isDone(), deadline);
    }
}
