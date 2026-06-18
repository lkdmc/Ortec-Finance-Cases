package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.Task;

import java.util.List;
import java.util.Map;

/**
 * API representation of one deadline group: a date (formatted dd-MM-yyyy, or
 * {@code null} for the tasks without a deadline) and the projects under it.
 */
public record DeadlineGroupResponse(String deadline, List<ProjectTasksResponse> projects) {
    public static DeadlineGroupResponse of(String deadline, Map<String, List<Task>> tasksByProject) {
        List<ProjectTasksResponse> projects = tasksByProject.entrySet().stream()
            .map(ProjectTasksResponse::from)
            .toList();
        return new DeadlineGroupResponse(deadline, projects);
    }
}
