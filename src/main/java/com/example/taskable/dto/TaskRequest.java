package com.example.taskable.dto;

import java.time.LocalDate;

import com.example.taskable.model.Task.Priority;
import com.example.taskable.model.Task.Status;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    private Long id;

    @NotBlank
    private String title;

    private String description;

    private LocalDate dueDate;

    private Priority priority;

    private Status status;
}
