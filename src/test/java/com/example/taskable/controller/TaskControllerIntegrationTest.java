package com.example.taskable.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.taskable.model.Task;
import com.example.taskable.model.Task.Priority;
import com.example.taskable.model.Task.Status;
import com.example.taskable.repository.TaskRepository;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TaskControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void postTasks_createsTask() throws Exception {
        String requestBody = """
                {
                  "title": "Create integration tests",
                  "description": "Cover the CRUD endpoints",
                  "dueDate": "2026-08-03",
                  "priority": "HIGH",
                  "status": "TODO"
                }
                """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Create integration tests"))
                .andExpect(jsonPath("$.description").value("Cover the CRUD endpoints"))
                .andExpect(jsonPath("$.dueDate").value("2026-08-03"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void getTasks_returnsAllTasks() throws Exception {
        taskRepository.save(Task.builder()
                .title("First task")
                .description("First description")
                .dueDate(LocalDate.of(2026, 8, 1))
                .priority(Priority.LOW)
                .status(Status.TODO)
                .build());
        taskRepository.save(Task.builder()
                .title("Second task")
                .description("Second description")
                .dueDate(LocalDate.of(2026, 8, 2))
                .priority(Priority.MEDIUM)
                .status(Status.IN_PROGRESS)
                .build());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getTaskById_returnsTask() throws Exception {
        Task savedTask = taskRepository.save(Task.builder()
                .title("Fetch task")
                .description("Verify GET by id")
                .dueDate(LocalDate.of(2026, 8, 4))
                .priority(Priority.HIGH)
                .status(Status.TODO)
                .build());

        mockMvc.perform(get("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Fetch task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.dueDate").value("2026-08-04"));
    }

    @Test
    void putTaskById_updatesTask() throws Exception {
        Task savedTask = taskRepository.save(Task.builder()
                .title("Old title")
                .description("Old description")
                .dueDate(LocalDate.of(2026, 8, 5))
                .priority(Priority.LOW)
                .status(Status.TODO)
                .build());

        String requestBody = """
                {
                  "id": %d,
                  "title": "Updated title",
                  "description": "Updated description",
                  "dueDate": "2026-08-06",
                  "priority": "HIGH",
                  "status": "DONE"
                }
                """.formatted(savedTask.getId());

        mockMvc.perform(put("/tasks/{id}", savedTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTask.getId()))
                .andExpect(jsonPath("$.title").value("Updated title"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.dueDate").value("2026-08-06"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void deleteTaskById_deletesTask() throws Exception {
        Task savedTask = taskRepository.save(Task.builder()
                .title("Delete me")
                .description("Verify DELETE endpoint")
                .dueDate(LocalDate.of(2026, 8, 7))
                .priority(Priority.MEDIUM)
                .status(Status.IN_PROGRESS)
                .build());

        mockMvc.perform(delete("/tasks/{id}", savedTask.getId()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
    }
}
