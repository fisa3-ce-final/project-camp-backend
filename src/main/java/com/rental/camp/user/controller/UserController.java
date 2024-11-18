package com.rental.camp.user.controller;

import com.rental.camp.user.dto.UserGetResponse;
import com.rental.camp.user.dto.UserModifyRequest;
import com.rental.camp.user.dto.UserModifyResponse;
import com.rental.camp.user.dto.UserSigninRequest;
import com.rental.camp.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    UserService userService;

    @PostMapping
    public ResponseEntity<String> signin(@RequestBody UserSigninRequest userSigninRequest, JwtAuthenticationToken principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body("signin failed");
        }
        userService.signIn(userSigninRequest, principal);
        return ResponseEntity.ok("signin success: " + principal.getName());
    }

    @GetMapping
    public ResponseEntity<UserGetResponse> me(JwtAuthenticationToken principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }

        UserGetResponse userGetResponse = userService.getUser(principal.getName());
        if (userGetResponse == null) {
            return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(userGetResponse);
    }

    @PutMapping
    public ResponseEntity<UserModifyResponse> updateUser(@ModelAttribute UserModifyRequest userModifyRequest,
                                                         JwtAuthenticationToken principal) throws IOException {
        if (principal == null) {
            return ResponseEntity.badRequest().body(null);
        }
        UserModifyResponse res = userService.updateUser(principal, userModifyRequest);
        return ResponseEntity.ok(res);
    }

    @DeleteMapping
    public ResponseEntity<String> signout(JwtAuthenticationToken principal) {
        if (principal == null) {
            return ResponseEntity.badRequest().body("signout failed");
        }
        userService.deleteUser(principal);
        return ResponseEntity.ok("delete success: " + principal.getName());
    }
}
