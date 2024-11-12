package com.rental.camp.user.controller;

import com.rental.camp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<String> signin(JwtAuthenticationToken principal) {
        String uuid = principal.getName();
        userService.signIn(uuid);

        return ResponseEntity.ok("signin success: " + uuid);
    }
}
