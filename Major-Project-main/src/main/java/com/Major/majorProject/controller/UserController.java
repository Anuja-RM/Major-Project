package com.Major.majorProject.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller()
@RequestMapping("/user")
public class UserController {

    @GetMapping("/findcafe")
    public String userPage(){
        return "user/userFindCafe";
    }

    @GetMapping("/cafedetails")
    public String cafeDetails(){
        return "user/userCafeDetails";
    }
}