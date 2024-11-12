package com.rental.camp.user.service;

import com.rental.camp.user.dto.UserGetResponse;
import com.rental.camp.user.dto.UserModifyRequest;
import com.rental.camp.user.dto.UserModifyResponse;
import com.rental.camp.user.model.User;
import com.rental.camp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    final UserRepository userRepository;

    @Override
    public void signIn(JwtAuthenticationToken principal) {
        UUID uuid = UUID.fromString(principal.getName());
        String email = principal.getTokenAttributes().get("email").toString();
        String picture = principal.getTokenAttributes().get("picture").toString();
        User exsistingUser = userRepository.findByUuid(uuid);
        if (exsistingUser == null) {
            User newUser = User.builder()
                    .uuid(uuid)
                    .email(email)
                    .nickname("닉네임_" + uuid.toString().split("-")[0])
                    .phone("")
                    .address("")
                    .imageUrl(picture)
                    .isDeleted(false)
                    .build();
            userRepository.save(newUser);
        } else {
            LocalDateTime date = LocalDateTime.now();

            exsistingUser.setUpdatedAt(date);
            userRepository.save(exsistingUser);
        }
    }

    @Override
    public UserGetResponse getUser(String _uuid) {
        UUID uuid = UUID.fromString(_uuid);
        User user = userRepository.findByUuid(uuid);
        if (user != null) {
            return UserGetResponse.builder()
                    .uuid(user.getUuid())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .nickname(user.getNickname())
                    .imageUrl(user.getImageUrl())
                    .provider(user.getProvider())
                    .createdAt(user.getCreatedAt())
                    .build();
        }

        return null;
    }

    @Override
    public UserModifyResponse updateUser(JwtAuthenticationToken principal, UserModifyRequest userModifyRequest) {
        UUID uuid = UUID.fromString(principal.getName());
        User user = userRepository.findByUuid(uuid);
        if (user != null) {
            user.setPhone(userModifyRequest.getPhone());
            user.setAddress(userModifyRequest.getAddress());
            user.setNickname(userModifyRequest.getNickname());
            userRepository.save(user);

            return UserModifyResponse.builder()
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .nickname(user.getNickname())
                    .imageUrl(user.getImageUrl())
                    .build();
        }

        return null;
    }

    @Override
    public void deleteUser(JwtAuthenticationToken principal) {
        UUID uuid = UUID.fromString(principal.getName());
        User user = userRepository.findByUuid(uuid);
        if (user != null) {
            userRepository.delete(user);
        }
    }
}
