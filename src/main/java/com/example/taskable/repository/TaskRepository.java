package com.example.taskable.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.taskable.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
