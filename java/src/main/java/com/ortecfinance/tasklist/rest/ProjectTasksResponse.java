package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.Task;

import java.util.List;
import java.util.Map;

/** API representation of a single project's tasks within a deadline group. */
public record ProjectTasksResponse(String name, List<TaskResponse> tasks) {
    public static ProjectTasksResponse from(Map.Entry<String, List<Task>> entry) {
        List<TaskResponse> tasks = entry.getValue().stream()
            .map(TaskResponse::from)
            .toList();
        return new ProjectTasksResponse(entry.getKey(), tasks);
    }
}
