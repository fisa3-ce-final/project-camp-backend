package com.rental.camp.user.controller;

import com.rental.camp.user.dto.UserGetResponse;
import com.rental.camp.user.dto.UserModifyRequest;
import com.rental.camp.user.dto.UserModifyResponse;
import com.rental.camp.user.dto.UserSigninRequest;
import com.rental.camp.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

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

    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    @GetMapping("/uuid")
    public ResponseEntity<Map<String, String>> getUserUuid(JwtAuthenticationToken principal) {
        String uuid = principal.getName();

        System.out.println(uuid + "11111111111111111111111111111111111");
        return ResponseEntity.ok(Map.of("uuid", uuid));
    }


}
