package com.example.taskable.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.taskable.dto.SubtaskBreakdown;
import com.example.taskable.dto.TaskSuggestion;
import com.example.taskable.service.AIService;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AiControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AIService aiService;

    @Test
    void postTasksSuggest_returnsSuggestedTaskJson() throws Exception {
        TaskSuggestion suggestion = new TaskSuggestion(
                "Draft presentation",
                "Prepare slides for the weekly update",
                "2026-08-04",
                "HIGH",
                "TODO"
        );
        when(aiService.suggestTask("Prepare a high priority presentation for next week")).thenReturn(suggestion);

        String requestBody = """
                {
                  "description": "Prepare a high priority presentation for next week"
                }
                """;

        mockMvc.perform(post("/tasks/suggest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Draft presentation"))
                .andExpect(jsonPath("$.description").value("Prepare slides for the weekly update"))
                .andExpect(jsonPath("$.dueDate").value("2026-08-04"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void postTaskSummarize_returnsNonEmptyString() throws Exception {
        when(aiService.summarizeTask(1L)).thenReturn(
                "This task is high priority and currently in progress. It is due on August 4, 2026, so it should stay near the top of the list."
        );

        mockMvc.perform(post("/tasks/{id}/summarize", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string(not(isEmptyOrNullString())));
    }

    @Test
    void postTaskBreakdown_returnsSubtasksJson() throws Exception {
        SubtaskBreakdown breakdown = new SubtaskBreakdown(List.of(
                "Gather requirements",
                "Draft the first version",
                "Review and revise"
        ));
        when(aiService.breakdownTask(1L)).thenReturn(breakdown);

        mockMvc.perform(post("/tasks/{id}/breakdown", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtasks", hasSize(3)))
                .andExpect(jsonPath("$.subtasks[0]").value("Gather requirements"))
                .andExpect(jsonPath("$.subtasks[1]").value("Draft the first version"))
                .andExpect(jsonPath("$.subtasks[2]").value("Review and revise"));
    }
}
