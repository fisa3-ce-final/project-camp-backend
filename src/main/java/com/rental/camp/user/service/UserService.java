package com.rental.camp.user.service;

import com.rental.camp.user.dto.UserGetResponse;
import com.rental.camp.user.dto.UserModifyRequest;
import com.rental.camp.user.dto.UserModifyResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public interface UserService {
    void signIn(JwtAuthenticationToken principal);

    UserGetResponse getUser(String _uuid);

    UserModifyResponse updateUser(JwtAuthenticationToken principal, UserModifyRequest userModifyRequest);

    void deleteUser(JwtAuthenticationToken principal);
}
