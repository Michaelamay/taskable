package com.example.taskable.service;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.taskable.dto.SubtaskBreakdown;
import com.example.taskable.dto.TaskSuggestion;
import com.example.taskable.model.Task;
import com.example.taskable.repository.TaskRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AIService {

    private final ClaudeService claudeService;
    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    public AIService(ClaudeService claudeService, TaskRepository taskRepository) {
        this.claudeService = claudeService;
        this.taskRepository = taskRepository;
        this.objectMapper = new ObjectMapper();
    }

    public TaskSuggestion suggestTask(String plainEnglishTaskDescription) {
        String prompt = """
                Convert the following plain-English task description into a task JSON object.
                Return only a JSON object with exactly these fields:
                title, description, dueDate, priority, status

                Rules:
                - dueDate must be in yyyy-MM-dd format or null
                - priority must be one of LOW, MEDIUM, HIGH
                - status must be one of TODO, IN_PROGRESS, DONE
                - Do not wrap the JSON in markdown fences
                - Do not include any explanation

                Task description:
                %s
                """.formatted(plainEnglishTaskDescription);

        String rawResponse = claudeService.sendMessage(prompt);
        String json = extractClaudeText(rawResponse);
        try {
            return objectMapper.readValue(json, TaskSuggestion.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Claude task suggestion response", e);
        }
    }

    public String summarizeTask(Long id) {
        Task task = getTaskOrThrow(id);

        String prompt = """
                Summarize this task in 2-3 conversational sentences.
                Include the priority, status, and due date.
                If the due date is missing, say that there is no due date yet.

                Task details:
                Title: %s
                Description: %s
                Due date: %s
                Priority: %s
                Status: %s
                """.formatted(
                task.getTitle(),
                task.getDescription() == null ? "" : task.getDescription(),
                task.getDueDate() == null ? "null" : task.getDueDate(),
                task.getPriority(),
                task.getStatus());

        String rawResponse = claudeService.sendMessage(prompt);
        return extractClaudeText(rawResponse);
    }

    public SubtaskBreakdown breakdownTask(Long id) {
        Task task = getTaskOrThrow(id);

        String prompt = """
                Break down the following task into practical subtasks.
                Return only a JSON object with a single field named subtasks.
                The subtasks field must be an array of strings.
                Do not include markdown fences or explanation.

                Task:
                Title: %s
                Description: %s
                Due date: %s
                Priority: %s
                Status: %s
                """.formatted(
                task.getTitle(),
                task.getDescription() == null ? "" : task.getDescription(),
                task.getDueDate() == null ? "null" : task.getDueDate(),
                task.getPriority(),
                task.getStatus());

        String rawResponse = claudeService.sendMessage(prompt);
        String json = extractClaudeText(rawResponse);
        try {
            return objectMapper.readValue(json, SubtaskBreakdown.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse Claude subtask breakdown response", e);
        }
    }

    private String extractClaudeText(String rawResponse) {
        try {
            JsonNode root = objectMapper.readTree(rawResponse);
            JsonNode content = root.path("content");
            if (content.isArray()) {
                StringBuilder builder = new StringBuilder();
                for (JsonNode node : content) {
                    if ("text".equals(node.path("type").asText())) {
                        if (builder.length() > 0) {
                            builder.append('\n');
                        }
                        builder.append(node.path("text").asText());
                    }
                }
                String text = builder.toString().trim();
                if (!text.isEmpty()) {
                    return stripMarkdownCodeFences(text);
                }
            }
        } catch (IOException e) {
            return stripMarkdownCodeFences(rawResponse.trim());
        }

        return stripMarkdownCodeFences(rawResponse.trim());
    }

    private Task getTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private String stripMarkdownCodeFences(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline >= 0) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }
}
