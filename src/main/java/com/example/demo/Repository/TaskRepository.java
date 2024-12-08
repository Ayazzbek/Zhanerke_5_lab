package com.example.demo.Repository;

import com.example.demo.Entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTaskStatusAndDueDateAndCategory_CategoryName(Task.TaskStatus taskStatus, LocalDate dueDate, String categoryName);

    List<Task> findByTaskStatusAndDueDateAndCategory_CategoryNameAndTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCase(
            Task.TaskStatus taskStatus, LocalDate dueDate, String categoryName, String title, String description);

    List<Task> findByTaskStatusAndDueDateAndCategory_CategoryNameAndUser_UserId(Task.TaskStatus taskStatus, LocalDate dueDate, String categoryName, Long userId);

    List<Task> findByTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCase(String title, String description);

    List<Task> findByTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCaseAndUser_UserId(String title, String description, Long userId);

    Page<Task> findByUser_UserId(Long userId, Pageable pageable);

    Task findByTaskIdAndUser_UserId(Long taskId, Long userId);

    List<Task> findByTaskStatusAndDueDateAndCategory_CategoryNameAndTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCaseAndUser_UserId(
            Task.TaskStatus taskStatus, LocalDate dueDate, String categoryName, String searchQuery, String searchQuery1, Long userId);

}
