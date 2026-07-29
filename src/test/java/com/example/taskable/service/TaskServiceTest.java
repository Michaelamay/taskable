package com.example.taskable.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.taskable.model.Task;
import com.example.taskable.model.Task.Priority;
import com.example.taskable.model.Task.Status;
import com.example.taskable.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = Task.builder()
                .id(1L)
                .title("Write tests")
                .description("Add unit coverage for task service")
                .dueDate(LocalDate.of(2026, 7, 31))
                .priority(Priority.HIGH)
                .status(Status.IN_PROGRESS)
                .build();
    }

    @Test
    void createTask_savesTaskAndClearsClientSuppliedId() {
        Task taskToCreate = Task.builder()
                .id(99L)
                .title("Create feature")
                .description("Draft initial implementation")
                .dueDate(LocalDate.of(2026, 8, 3))
                .priority(Priority.MEDIUM)
                .status(Status.TODO)
                .build();

        when(taskRepository.save(taskToCreate)).thenReturn(task);

        Task createdTask = taskService.createTask(taskToCreate);

        assertNotNull(createdTask);
        assertEquals(task, createdTask);
        assertNull(taskToCreate.getId());
        verify(taskRepository).save(taskToCreate);
    }

    @Test
    void getAllTasks_returnsAllTasks() {
        List<Task> tasks = List.of(
                task,
                Task.builder()
                        .id(2L)
                        .title("Review PR")
                        .description("Check controller changes")
                        .dueDate(LocalDate.of(2026, 8, 1))
                        .priority(Priority.LOW)
                        .status(Status.TODO)
                        .build()
        );
        when(taskRepository.findAll()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        assertEquals(tasks, result);
        verify(taskRepository).findAll();
    }

    @Test
    void getTaskById_returnsTaskWhenFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        Task result = taskService.getTaskById(1L);

        assertEquals(task, result);
        verify(taskRepository).findById(1L);
    }

    @Test
    void updateTask_updatesExistingTaskAndSavesIt() {
        Task updatedTask = Task.builder()
                .title("Write more tests")
                .description("Cover happy path and edge cases")
                .dueDate(LocalDate.of(2026, 8, 5))
                .priority(Priority.MEDIUM)
                .status(Status.DONE)
                .build();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        Task result = taskService.updateTask(1L, updatedTask);

        assertEquals("Write more tests", result.getTitle());
        assertEquals("Cover happy path and edge cases", result.getDescription());
        assertEquals(LocalDate.of(2026, 8, 5), result.getDueDate());
        assertEquals(Priority.MEDIUM, result.getPriority());
        assertEquals(Status.DONE, result.getStatus());
        verify(taskRepository).findById(1L);
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_deletesExistingTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).findById(1L);
        verify(taskRepository, times(1)).delete(task);
    }
}
