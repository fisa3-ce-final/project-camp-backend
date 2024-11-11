package com.rental.camp;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    @PreAuthorize("hasRole('USER')")
    public String user(JwtAuthenticationToken principal) {
        String email = principal.getTokenAttributes().get("email").toString();
        String picture = principal.getTokenAttributes().get("picture").toString();
        String name = principal.getName();
        return "Hello, " + email;
    }

    @GetMapping("/")
    public String rooturl() {
        return "index ";
    }

}
