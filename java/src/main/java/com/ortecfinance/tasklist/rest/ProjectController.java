package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.Project;
import com.ortecfinance.tasklist.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST endpoints for projects and their tasks.
 *
 * <p>Delegates to the same {@link TaskService} that backs the console, so the
 * behavior stays consistent across interfaces.
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {
    private final TaskService service;

    public ProjectController(TaskService service) {
        this.service = service;
    }

    /** Returns all projects and their underlying tasks. */
    @GetMapping
    public List<ProjectResponse> getProjects() {
        return service.getProjects().stream()
            .map(ProjectResponse::from)
            .toList();
    }

    /** Creates a new project and returns it, including its generated id. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse createProject(@RequestBody CreateProjectRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Project name is required.");
        }
        Project project = service.addProject(request.name());
        return ProjectResponse.from(project);
    }
}
