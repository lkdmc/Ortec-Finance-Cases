package com.ortecfinance.tasklist.rest;

import com.ortecfinance.tasklist.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for {@link ProjectController}. Each test gets a fresh
 * controller and service through a standalone MockMvc setup, so the cases are
 * isolated and the generated ids are predictable.
 */
class ProjectControllerTest {
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ProjectController(new TaskService())).build();
    }

    @Test
    void creates_and_lists_projects() throws Exception {
        mockMvc.perform(post("/projects").contentType(APPLICATION_JSON).content("{\"name\":\"secrets\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("secrets"))
            .andExpect(jsonPath("$.tasks").isEmpty());

        mockMvc.perform(get("/projects"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("secrets"));
    }

    @Test
    void rejects_a_project_without_a_name() throws Exception {
        mockMvc.perform(post("/projects").contentType(APPLICATION_JSON).content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adds_a_task_to_a_project() throws Exception {
        createProject("secrets");

        mockMvc.perform(post("/projects/1/tasks").contentType(APPLICATION_JSON)
                .content("{\"description\":\"Eat more donuts.\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.description").value("Eat more donuts."))
            .andExpect(jsonPath("$.done").value(false))
            .andExpect(jsonPath("$.deadline").isEmpty());
    }

    @Test
    void returns_404_when_adding_a_task_to_an_unknown_project() throws Exception {
        mockMvc.perform(post("/projects/99/tasks").contentType(APPLICATION_JSON)
                .content("{\"description\":\"Nope\"}"))
            .andExpect(status().isNotFound());
    }

    @Test
    void updates_a_task_deadline() throws Exception {
        createProject("secrets");
        createTask(1, "Eat more donuts.");

        mockMvc.perform(put("/projects/1/tasks/1").param("deadline", "25-11-2024"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deadline").value("25-11-2024"));
    }

    @Test
    void rejects_an_invalid_deadline() throws Exception {
        createProject("secrets");
        createTask(1, "Eat more donuts.");

        mockMvc.perform(put("/projects/1/tasks/1").param("deadline", "not-a-date"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void views_tasks_grouped_by_deadline_with_no_deadline_last() throws Exception {
        createProject("secrets");
        createTask(1, "Eat more donuts.");
        createTask(1, "Refactor the codebase");
        mockMvc.perform(put("/projects/1/tasks/1").param("deadline", "11-11-2021"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/projects/view_by_deadline"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].deadline").value("11-11-2021"))
            .andExpect(jsonPath("$[0].projects[0].name").value("secrets"))
            .andExpect(jsonPath("$[0].projects[0].tasks[0].id").value(1))
            .andExpect(jsonPath("$[1].deadline").isEmpty())
            .andExpect(jsonPath("$[1].projects[0].tasks[0].id").value(2));
    }

    private void createProject(String name) throws Exception {
        mockMvc.perform(post("/projects").contentType(APPLICATION_JSON)
                .content("{\"name\":\"" + name + "\"}"))
            .andExpect(status().isCreated());
    }

    private void createTask(long projectId, String description) throws Exception {
        mockMvc.perform(post("/projects/" + projectId + "/tasks").contentType(APPLICATION_JSON)
                .content("{\"description\":\"" + description + "\"}"))
            .andExpect(status().isCreated());
    }
}
