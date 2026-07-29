package com.example.taskable.controller;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.taskable.dto.SubtaskBreakdown;
import com.example.taskable.dto.TaskRequest;
import com.example.taskable.dto.TaskSuggestion;
import com.example.taskable.dto.TaskSuggestionRequest;
import com.example.taskable.model.Task;
import com.example.taskable.service.AIService;
import com.example.taskable.service.TaskService;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final AIService aiService;

    public TaskController(TaskService taskService, AIService aiService) {
        this.taskService = taskService;
        this.aiService = aiService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@Valid @RequestBody TaskRequest request) {
        Task task = toTask(request);
        task.setId(null);
        return taskService.createTask(task);
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
        if (request.getId() != null && !id.equals(request.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task id in body must match path id");
        }
        return taskService.updateTask(id, toTask(request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PostMapping("/suggest")
    public TaskSuggestion suggestTask(@Valid @RequestBody TaskSuggestionRequest request) {
        return aiService.suggestTask(request.getDescription());
    }

    @PostMapping("/{id}/summarize")
    public String summarizeTask(@PathVariable Long id) {
        return aiService.summarizeTask(id);
    }

    @PostMapping("/{id}/breakdown")
    public SubtaskBreakdown breakdownTask(@PathVariable Long id) {
        return aiService.breakdownTask(id);
    }

    private Task toTask(TaskRequest request) {
        return Task.builder()
                .id(request.getId())
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .status(request.getStatus())
                .build();
    }
}
