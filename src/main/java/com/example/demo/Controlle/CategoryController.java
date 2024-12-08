package com.example.demo.Controlle;



import com.example.demo.Entity.Category;
import com.example.demo.Service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize(" hasRole('ADMIN')")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "category";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String showCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "new_category";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveCategory(@ModelAttribute("category") Category category) {
        categoryService.saveCategory(category);
        return "redirect:/categories";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String showEditCategoryForm(@PathVariable("id") Long id, Model model) {
        Category category = categoryService.getCategoryById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + id));
        model.addAttribute("category", category);
        return "update_category";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCategory(@PathVariable("id") Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/categories";
    }

    @GetMapping("/page/{pageNo}")
    @PreAuthorize(" hasRole('ADMIN')")
    public String findCategoryPaginated(@PathVariable(value = "pageNo") int pageNo, Model model) {
        int pageSize = 5;
        Page<Category> page = categoryService.findCategoryPaginated(pageNo, pageSize);
        List<Category> listCategories = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("listCategories", listCategories);

        return "category";
    }
}