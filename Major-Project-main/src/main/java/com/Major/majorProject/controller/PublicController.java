package com.Major.majorProject.controller;

import com.Major.majorProject.dto.OwnerRegistrationDto;
import com.Major.majorProject.dto.UserRegistrationDto;
import com.Major.majorProject.service.OwnerService;
import com.Major.majorProject.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PublicController {

    private final OwnerService ownerService;
    private final UserService userService;

    public PublicController(OwnerService os, UserService userService) {
        this.ownerService = os;
        this.userService = userService;
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
        return "redirect:/login";
    }

    @GetMapping("/register/user")
    public String showUserRegistrationForm(@RequestParam(value = "pcId", required = false) Long pcId,
                                           @RequestParam(value = "startTime", required = false) String startTime,
                                           Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        model.addAttribute("pcId", pcId);
        model.addAttribute("startTime", startTime);
        return "user/userRegistration";
    }

    @PostMapping("/register/user")
    public String registerUserAccount(@ModelAttribute("user") UserRegistrationDto registrationDto,
                                      @RequestParam(value = "pcId", required = false) Long pcId,
                                      @RequestParam(value = "startTime", required = false) String startTime) {
        userService.userRegistration(registrationDto);

        if (pcId != null && startTime != null) {
            return "redirect:/login?pcId=" + pcId + "&startTime=" + startTime;
        }
        return "redirect:/login";
    }
}