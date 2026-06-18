package com.ortecfinance.tasklist;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
    private long lastTaskId = 0;
    private long lastProjectId = 0;

    /**
     * Creates a new, empty project.
     *
     * @return the created project, including its generated id.
     */
    public Project addProject(String name) {
        Project project = new Project(nextProjectId(), name);
        projects.put(name, project);
        return project;
    }

    /**
     * Adds a task to an existing project, located by name.
     *
     * @return the created task, or {@link Optional#empty()} if no project has the given name.
     */
    public Optional<Task> addTask(String projectName, String description) {
        return addTaskTo(projects.get(projectName), description);
    }

    /**
     * Adds a task to an existing project, located by id.
     *
     * @return the created task, or {@link Optional#empty()} if no project has the given id.
     */
    public Optional<Task> addTask(long projectId, String description) {
        return addTaskTo(findProjectById(projectId), description);
    }

    private Optional<Task> addTaskTo(Project project, String description) {
        if (project == null) {
            return Optional.empty();
        }
        Task task = new Task(nextTaskId(), description, false);
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
     * @return the updated task, or {@link Optional#empty()} if no task has that id.
     */
    public Optional<Task> setDeadline(long id, LocalDate deadline) {
        Task task = findById(id);
        if (task == null) {
            return Optional.empty();
        }
        task.setDeadline(deadline);
        return Optional.of(task);
    }

    /** Returns all projects in creation order, as an unmodifiable view. */
    public Collection<Project> getProjects() {
        return Collections.unmodifiableCollection(projects.values());
    }

    /**
     * Groups the tasks that have a deadline, by deadline and then by project name.
     *
     * <p>The outer map is ordered chronologically by deadline, and within each
     * deadline the projects keep their original creation order.
     */
    public Map<LocalDate, Map<String, List<Task>>> tasksByDeadline() {
        Map<LocalDate, Map<String, List<Task>>> grouped = new TreeMap<>();
        for (Project project : projects.values()) {
            for (Task task : project.getTasks()) {
                if (task.getDeadline() != null) {
                    grouped
                        .computeIfAbsent(task.getDeadline(), date -> new LinkedHashMap<>())
                        .computeIfAbsent(project.getName(), name -> new ArrayList<>())
                        .add(task);
                }
            }
        }
        return grouped;
    }

    /** Groups the tasks that have no deadline, by project name in creation order. */
    public Map<String, List<Task>> tasksWithoutDeadline() {
        Map<String, List<Task>> grouped = new LinkedHashMap<>();
        for (Project project : projects.values()) {
            for (Task task : project.getTasks()) {
                if (task.getDeadline() == null) {
                    grouped.computeIfAbsent(project.getName(), name -> new ArrayList<>()).add(task);
                }
            }
        }
        return grouped;
    }

    private Project findProjectById(long id) {
        for (Project project : projects.values()) {
            if (project.getId() == id) {
                return project;
            }
        }
        return null;
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

    private long nextTaskId() {
        return ++lastTaskId;
    }

    private long nextProjectId() {
        return ++lastProjectId;
    }
}
