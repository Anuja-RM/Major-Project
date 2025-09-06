package com.Major.majorProject.controller;

import com.Major.majorProject.dto.OwnerRegistrationDto;
import com.Major.majorProject.service.OwnerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PublicController {

    private final OwnerService ownerService;

    public PublicController(OwnerService os) {
        this.ownerService = os;
    }

    @GetMapping("/landing")
    public String landing(){
        return "landing";
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "owner/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model){
        model.addAttribute("ownerRegistrationDto", new OwnerRegistrationDto());
        return "owner/ownerRegister";
    }

    @PostMapping("/register")
    public String ownerRegistration(@ModelAttribute("ownerRegistrationDto") OwnerRegistrationDto ord){
        System.out.println("Received registration request for email: " + ord.getEmail());
        ownerService.ownerRegistration(ord);
        return "redirect:owner/login";
    }
}