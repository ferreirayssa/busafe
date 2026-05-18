package com.transcol.busafe.controller;

import com.transcol.busafe.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String home(Model model) {
        long total = userService.allUsers();

        model.addAttribute("allUsers", total);

        return "index";
    }
}