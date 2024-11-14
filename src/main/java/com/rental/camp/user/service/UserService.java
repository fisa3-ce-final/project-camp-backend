package com.rental.camp.user.service;

import com.rental.camp.user.dto.UserGetResponse;
import com.rental.camp.user.dto.UserModifyRequest;
import com.rental.camp.user.dto.UserModifyResponse;
import com.rental.camp.user.dto.UserSigninRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.io.IOException;

public interface UserService {
    void signIn(UserSigninRequest signinRequest, JwtAuthenticationToken principal);

    UserGetResponse getUser(String _uuid);

    UserModifyResponse updateUser(JwtAuthenticationToken principal, UserModifyRequest userModifyRequest) throws IOException;

    void deleteUser(JwtAuthenticationToken principal);
}
