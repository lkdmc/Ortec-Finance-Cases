package com.ortecfinance.tasklist;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the interface-agnostic core logic. These exercise
 * {@link TaskService} directly, without going through any console plumbing.
 */
class TaskServiceTest {

    private final TaskService service = new TaskService();

    @Test
    void adds_a_task_to_an_existing_project() {
        service.addProject("secrets");

        Optional<Task> added = service.addTask("secrets", "Eat more donuts.");

        assertTrue(added.isPresent());
        assertEquals("Eat more donuts.", added.get().getDescription());

        List<Task> tasks = onlyProject().getTasks();
        assertEquals(1, tasks.size());
        assertSame(added.get(), tasks.get(0));
    }

    @Test
    void does_not_add_a_task_when_the_project_is_unknown() {
        Optional<Task> added = service.addTask("missing", "Nope");

        assertTrue(added.isEmpty());
        assertTrue(service.getProjects().isEmpty());
    }

    @Test
    void assigns_sequential_ids_across_projects() {
        service.addProject("a");
        service.addProject("b");

        long first = service.addTask("a", "one").orElseThrow().getId();
        long second = service.addTask("b", "two").orElseThrow().getId();
        long third = service.addTask("a", "three").orElseThrow().getId();

        assertEquals(1, first);
        assertEquals(2, second);
        assertEquals(3, third);
    }

    @Test
    void assigns_sequential_project_ids() {
        assertEquals(1, service.addProject("a").getId());
        assertEquals(2, service.addProject("b").getId());
        assertEquals(3, service.addProject("c").getId());
    }

    @Test
    void checks_and_unchecks_a_task() {
        long id = givenATask();

        assertTrue(service.setDone(id, true));
        assertTrue(findTask(id).isDone());

        assertTrue(service.setDone(id, false));
        assertFalse(findTask(id).isDone());
    }

    @Test
    void reports_when_setting_done_on_an_unknown_task() {
        assertFalse(service.setDone(99, true));
    }

    @Test
    void adds_a_task_to_a_project_by_id() {
        long projectId = service.addProject("secrets").getId();

        Optional<Task> added = service.addTask(projectId, "Eat more donuts.");

        assertTrue(added.isPresent());
        assertEquals("Eat more donuts.", added.get().getDescription());
    }

    @Test
    void does_not_add_a_task_when_the_project_id_is_unknown() {
        assertTrue(service.addTask(99L, "Nope").isEmpty());
    }

    @Test
    void sets_a_deadline_on_an_existing_task() {
        long id = givenATask();
        LocalDate deadline = LocalDate.of(2024, 11, 25);

        Optional<Task> updated = service.setDeadline(id, deadline);

        assertTrue(updated.isPresent());
        assertEquals(deadline, updated.get().getDeadline());
    }

    @Test
    void reports_when_setting_a_deadline_on_an_unknown_task() {
        assertTrue(service.setDeadline(99, LocalDate.now()).isEmpty());
    }

    @Test
    void keeps_projects_in_creation_order() {
        service.addProject("first");
        service.addProject("second");
        service.addProject("third");

        List<String> names = service.getProjects().stream().map(Project::getName).toList();
        assertEquals(List.of("first", "second", "third"), names);
    }

    @Test
    void exposes_projects_as_an_unmodifiable_collection() {
        service.addProject("a");
        assertThrows(UnsupportedOperationException.class, () -> service.getProjects().clear());
    }

    private long givenATask() {
        service.addProject("a");
        return service.addTask("a", "one").orElseThrow().getId();
    }

    private Project onlyProject() {
        return service.getProjects().iterator().next();
    }

    private Task findTask(long id) {
        return service.getProjects().stream()
            .flatMap(project -> project.getTasks().stream())
            .filter(task -> task.getId() == id)
            .findFirst()
            .orElseThrow();
    }
}
