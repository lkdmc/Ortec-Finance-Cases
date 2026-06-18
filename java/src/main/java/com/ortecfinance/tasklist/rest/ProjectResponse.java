package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.Project;

import java.util.List;

/** API representation of a project together with its tasks. */
public record ProjectResponse(long id, String name, List<TaskResponse> tasks) {
    public static ProjectResponse from(Project project) {
        List<TaskResponse> tasks = project.getTasks().stream()
            .map(TaskResponse::from)
            .toList();
        return new ProjectResponse(project.getId(), project.getName(), tasks);
    }
}
