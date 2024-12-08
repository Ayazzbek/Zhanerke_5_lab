package com.example.demo.Controlle;

import com.example.demo.Entity.Category;
import com.example.demo.Entity.Task;
import com.example.demo.Entity.User;
import com.example.demo.Service.CategoryService;
import com.example.demo.Service.TaskService;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;
    private final CategoryService categoryService;

    @Autowired
    public TaskController(TaskService taskService, UserService userService, CategoryService categoryService) {
        this.taskService = taskService;
        this.userService = userService;
        this.categoryService = categoryService;
    }

    private User getCurrentUser(Principal principal) {
        return userService.findByEmail(principal.getName());
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String getTasks(@RequestParam(value = "status", required = false) Task.TaskStatus taskStatus,
                           @RequestParam(value = "dueDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate dueDate,
                           @RequestParam(value = "categoryName", required = false) String categoryName,
                           @RequestParam(value = "searchQuery", required = false) String searchQuery,
                           Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        List<Task> tasks;

        if (currentUser.getRole() == User.Role.ADMIN) {
            tasks = taskService.getTasksWithFiltersForAdmin(taskStatus, dueDate, categoryName, searchQuery);
        } else {
            tasks = taskService.getTasksWithFiltersForUser(taskStatus, dueDate, categoryName, searchQuery, currentUser.getUserId());
        }

        if (tasks.isEmpty()) {
            model.addAttribute("message", "No tasks found.");
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("user", currentUser);
        model.addAttribute("searchQuery", searchQuery); // Ensure search query appears in the input field
        return "task";
    }

    @GetMapping("/page/{pageNo}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String getPaginatedTasks(@PathVariable int pageNo,
                                    @RequestParam(defaultValue = "5") int pageSize,
                                    Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        Page<Task> tasksPage;

        if (currentUser.getRole() == User.Role.ADMIN) {
            tasksPage = taskService.getPaginatedTasksForAdmin(pageNo - 1, pageSize);
        } else {
            tasksPage = taskService.getPaginatedTasksForUser(currentUser.getUserId(), pageNo - 1, pageSize);
        }

        model.addAttribute("tasks", tasksPage.getContent());
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", tasksPage.getTotalPages());
        model.addAttribute("user", currentUser);
        return "task";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showNewTaskForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("users", userService.getAllUsers());
        return "new_task";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveTask(@ModelAttribute Task task,
                           @RequestParam("userId") Long userId,
                           @RequestParam("categoryId") Long categoryId,
                           @RequestParam("status") Task.TaskStatus status) {

        User selectedUser = userService.getUserById(userId);
        if (selectedUser == null) {
            throw new RuntimeException("Selected user not found.");
        }

        Optional<Category> optionalCategory = categoryService.getCategoryById(categoryId);
        if (optionalCategory.isPresent()) {
            task.setCategory(optionalCategory.get());
        } else {
            throw new RuntimeException("Selected category not found.");
        }

        task.setTaskStatus(status);
        task.setUser(selectedUser);
        taskService.saveTask(task);
        return "redirect:/tasks/list";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditTaskForm(@PathVariable Long id, Model model) {
        Task task = taskService.getTaskById(id);
        if (task != null) {
            model.addAttribute("task", task);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("users", userService.getAllUsers());
        }
        return "update_task";
    }

    @PostMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateTask(@PathVariable Long id, @ModelAttribute Task task) {
        taskService.updateTask(id, task);
        return "redirect:/tasks/list";
    }

    @PostMapping("/status/{id}")
    @PreAuthorize("hasRole('USER')")
    public String updateTaskStatus(@PathVariable Long id,
                                   @RequestParam("status") Task.TaskStatus status,
                                   Principal principal) {
        User currentUser = getCurrentUser(principal);
        taskService.updateTaskStatus(id, currentUser.getUserId(), status);
        return "redirect:/tasks";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/tasks/list";
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String searchTasks(@RequestParam("query") String query,
                              Model model, Principal principal) {
        User currentUser = getCurrentUser(principal);
        List<Task> tasks;

        if (currentUser.getRole() == User.Role.ADMIN) {
            tasks = taskService.searchTasksForAdmin(query);
        } else {
            tasks = taskService.searchTasksForUser(query, currentUser.getUserId());
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("user", currentUser);
        return "task";
    }
}

