package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.Project;
import com.ortecfinance.tasklist.Task;
import com.ortecfinance.tasklist.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    private static final DateTimeFormatter DEADLINE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

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

    /** Creates a new task within a project and returns it, including its generated id. */
    @PostMapping("/{projectId}/tasks")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@PathVariable long projectId, @RequestBody CreateTaskRequest request) {
        if (request == null || request.description() == null || request.description().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task description is required.");
        }
        Task task = service.addTask(projectId, request.description())
            .orElseThrow(() -> projectNotFound(projectId));
        return TaskResponse.from(task);
    }

    /** Updates the deadline of a task and returns the updated task. */
    @PutMapping("/{projectId}/tasks/{taskId}")
    public TaskResponse updateDeadline(@PathVariable long projectId,
                                       @PathVariable long taskId,
                                       @RequestParam String deadline) {
        LocalDate date = parseDeadline(deadline);
        Task task = service.setDeadline(taskId, date)
            .orElseThrow(() -> taskNotFound(taskId));
        return TaskResponse.from(task);
    }

    private LocalDate parseDeadline(String value) {
        try {
            return LocalDate.parse(value, DEADLINE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Deadline must use the format dd-MM-yyyy.");
        }
    }

    private ResponseStatusException projectNotFound(long projectId) {
        return new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Could not find a project with the id " + projectId + ".");
    }

    private ResponseStatusException taskNotFound(long taskId) {
        return new ResponseStatusException(
            HttpStatus.NOT_FOUND, "Could not find a task with the id " + taskId + ".");
    }
}
