package com.example.webbanhang.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GoogleLoginController {
    @GetMapping("/login_google")
    public String getUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            return "User: " + principal.getAttribute("name");
        }
        return "User not found!";
    }
}
