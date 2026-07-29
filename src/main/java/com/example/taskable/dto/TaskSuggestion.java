package com.example.taskable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskSuggestion {

    private String title;
    private String description;
    private String dueDate;
    private String priority;
    private String status;
}
