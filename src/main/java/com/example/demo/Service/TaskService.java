package com.example.demo.Service;

import com.example.demo.Entity.Task;
import com.example.demo.Repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    @Autowired
    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    // Get tasks with filters for admin
    public List<Task> getTasksWithFiltersForAdmin(Task.TaskStatus taskStatus, LocalDate dueDate, String categoryName, String searchQuery) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return taskRepository.findByTaskStatusAndDueDateAndCategory_CategoryNameAndTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCase(
                    taskStatus, dueDate, categoryName, searchQuery, searchQuery);
        } else {
            return taskRepository.findByTaskStatusAndDueDateAndCategory_CategoryName(taskStatus, dueDate, categoryName);
        }
    }

    // Get tasks with filters for users
    public List<Task> getTasksWithFiltersForUser(Task.TaskStatus taskStatus, LocalDate dueDate, String categoryName, String searchQuery, Long userId) {
        if (searchQuery != null && !searchQuery.isEmpty()) {
            return taskRepository.findByTaskStatusAndDueDateAndCategory_CategoryNameAndTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCaseAndUser_UserId(
                    taskStatus, dueDate, categoryName, searchQuery, searchQuery, userId);
        } else {
            return taskRepository.findByTaskStatusAndDueDateAndCategory_CategoryNameAndUser_UserId(taskStatus, dueDate, categoryName, userId);
        }
    }

    // Get tasks with pagination for admin
    public Page<Task> getPaginatedTasksForAdmin(int page, int size) {
        return taskRepository.findAll(PageRequest.of(page, size));
    }

    // Get tasks with pagination for users
    public Page<Task> getPaginatedTasksForUser(Long userId, int page, int size) {
        return taskRepository.findByUser_UserId(userId, PageRequest.of(page, size));
    }

    // Search tasks for admin
    public List<Task> searchTasksForAdmin(String query) {
        return taskRepository.findByTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCase(query, query);
    }

    // Search tasks for user
    public List<Task> searchTasksForUser(String query, Long userId) {
        return taskRepository.findByTitleContainingIgnoreCaseOrTaskDescriptionContainingIgnoreCaseAndUser_UserId(query, query, userId);
    }

    // Save task
    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    // Get task by ID
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    // Update task
    public void updateTask(Long id, Task updatedTask) {
        Task task = getTaskById(id);
        if (task != null) {
            task.setTitle(updatedTask.getTitle());
            task.setTaskDescription(updatedTask.getTaskDescription());
            task.setTaskStatus(updatedTask.getTaskStatus());
            task.setCategory(updatedTask.getCategory());
            task.setDueDate(updatedTask.getDueDate());
            taskRepository.save(task);
        }
    }

    // Update task status
    public void updateTaskStatus(Long taskId, Long userId, Task.TaskStatus taskStatus) {
        Task task = taskRepository.findByTaskIdAndUser_UserId(taskId, userId);
        if (task != null) {
            task.setTaskStatus(taskStatus);
            taskRepository.save(task);
        }
    }

    // Delete task
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

}
