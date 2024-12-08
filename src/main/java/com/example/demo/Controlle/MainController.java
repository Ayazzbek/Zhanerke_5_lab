package com.example.demo.Controlle;


import com.example.demo.Entity.User;
import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class MainController {

    private final UserService usersService;

    @Autowired
    public MainController(UserService userService) {
        this.usersService = userService;
    }

    @GetMapping("/")
    public String mainPage(Principal principal, Model model) {
        String email = principal.getName();
        User user = usersService.findByEmail(email);
        model.addAttribute("user", user);
        return "main";
    }
}
