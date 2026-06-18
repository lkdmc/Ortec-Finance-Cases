package com.ortecfinance.tasklist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A named project that groups the tasks created under it.
 *
 * <p>The project owns its task list: callers add tasks through {@link #addTask(Task)}
 * and can only read the list back as an unmodifiable view, so the collection cannot
 * be mutated from the outside.
 */
public final class Project {
    private final String name;
    private final List<Task> tasks = new ArrayList<>();

    public Project(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    /** Returns the project's tasks in creation order, as an unmodifiable view. */
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(Task task) {
        tasks.add(task);
    }
}
