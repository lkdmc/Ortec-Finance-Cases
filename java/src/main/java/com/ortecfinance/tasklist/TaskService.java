package com.ortecfinance.tasklist;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Core task-management logic, independent of any user interface.
 *
 * <p>Owns the projects and their tasks in memory and exposes operations that
 * return results instead of printing. This lets the same logic back the console
 * application, a future REST API, or any other interface. Reporting outcomes to
 * the user is the responsibility of the calling interface.
 */
public final class TaskService {
    private final Map<String, Project> projects = new LinkedHashMap<>();
    private long lastId = 0;

    /** Creates a new, empty project. */
    public void addProject(String name) {
        projects.put(name, new Project(name));
    }

    /**
     * Adds a task to an existing project.
     *
     * @return the created task, or {@link Optional#empty()} if no project has the given name.
     */
    public Optional<Task> addTask(String projectName, String description) {
        Project project = projects.get(projectName);
        if (project == null) {
            return Optional.empty();
        }
        Task task = new Task(nextId(), description, false);
        project.addTask(task);
        return Optional.of(task);
    }

    /**
     * Sets the done flag of the task with the given id.
     *
     * @return {@code true} if a task with that id existed.
     */
    public boolean setDone(long id, boolean done) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }
        task.setDone(done);
        return true;
    }

    /**
     * Sets the deadline of the task with the given id.
     *
     * @return {@code true} if a task with that id existed.
     */
    public boolean setDeadline(long id, LocalDate deadline) {
        Task task = findById(id);
        if (task == null) {
            return false;
        }
        task.setDeadline(deadline);
        return true;
    }

    /** Returns all projects in creation order, as an unmodifiable view. */
    public Collection<Project> getProjects() {
        return Collections.unmodifiableCollection(projects.values());
    }

    private Task findById(long id) {
        for (Project project : projects.values()) {
            for (Task task : project.getTasks()) {
                if (task.getId() == id) {
                    return task;
                }
            }
        }
        return null;
    }

    private long nextId() {
        return ++lastId;
    }
}
