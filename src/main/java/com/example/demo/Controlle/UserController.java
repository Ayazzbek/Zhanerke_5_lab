package com.example.demo.Controlle;


import com.example.demo.Entity.User;
import com.example.demo.Service.EmailService;
import com.example.demo.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserController(UserService userService, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                               BindingResult result,
                               Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userService.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email уже зарегистрирован");
            return "register";
        }

        if (userService.getAllUsers().isEmpty()) {
            user.setRole(User.Role.ADMIN);
        } else {
            user.setRole(User.Role.USER);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        user.setProfilePicture("/images/default-avatar.png");

        userService.saveUser(user);

        String subject = "Welcome to our Website!";
        String text = "Hello " + user.getUserName() + ",\n\nThank you for registering on our website!\n\nBest regards,\nYour Website Team";

        emailService.sendEmail(user.getEmail(), subject, text);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "user";
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String showUserProfile(Model model, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            throw new RuntimeException("User not found for email: " + principal.getName());
        }
        model.addAttribute("user", currentUser);
        return "profile";
    }

    @GetMapping("/profile/edit")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String editProfile(Model model, Principal principal) {
        User currentUser = userService.findByEmail(principal.getName());
        if (currentUser == null) {
            return "redirect:/user/profile";
        }
        currentUser.setPassword("");
        model.addAttribute("user", currentUser);
        return "update_profile";
    }

    @PostMapping("/profile/update")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public String updateProfile(@Valid @ModelAttribute("user") User user,
                                BindingResult result,
                                @RequestParam("profilePicture") MultipartFile profilePicture,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "update_profile";
        }


        User existingUser = userService.findByEmail(principal.getName());


        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }


        existingUser.setUserName(user.getUserName());


        if (!profilePicture.isEmpty()) {
            try {
                // Папка для сохранения изображений
                String uploadDir = "src/main/resources/static/images/";
                String fileName = UUID.randomUUID() + "_" + profilePicture.getOriginalFilename();
                Path filePath = Paths.get(uploadDir + fileName);
                Files.write(filePath, profilePicture.getBytes());


                existingUser.setProfilePicture("/images/" + fileName);
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload the image.");
                return "redirect:/user/profile";
            }
        }

        if (existingUser.getProfilePicture() == null || existingUser.getProfilePicture().isEmpty()) {
            existingUser.setProfilePicture("/images/default-avatar.png");
        }

        userService.saveUser(existingUser);

        return "redirect:/user/profile";
    }



    @GetMapping("/edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String editUserForm(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/user/list";
        }
        model.addAttribute("user", user);
        return "update_user";
    }

    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateUser(@RequestParam Long userId, @RequestParam String role) {
        User existingUser = userService.getUserById(userId);

        if (existingUser == null) {
            return "redirect:/user/list";
        }

        existingUser.setRole(User.Role.valueOf(role));
        userService.saveUser(existingUser);

        return "redirect:/user/list";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/user/list";
    }
    @GetMapping("/page/{pageNo}")
    @PreAuthorize("hasRole('ADMIN')")
    public String findPaginated(@PathVariable("pageNo") int pageNo, Model model) {
        int pageSize = 5;
        Page<User> page = userService.findPaginated(pageNo, pageSize);
        List<User> listUsers = page.getContent();

        model.addAttribute("currentPage", pageNo);
        model.addAttribute("totalPages", page.getTotalPages());
        model.addAttribute("totalItems", page.getTotalElements());
        model.addAttribute("listUsers", listUsers);

        return "user";
    }
}