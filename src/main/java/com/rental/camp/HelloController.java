package com.rental.camp;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String user(@AuthenticationPrincipal OidcUser oidcUser) {
        return "Hello, " + oidcUser.getEmail();
    }

    @GetMapping("/")
    public String rooturl() {
        return "wow ";
    }

}
