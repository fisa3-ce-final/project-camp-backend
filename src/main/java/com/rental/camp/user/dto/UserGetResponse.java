package com.rental.camp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGetResponse {
    private UUID uuid;
    private String username;
    private String email;
    private String phone;
    private String address;
    private String nickname;
    private String imageUrl;
    private String provider;
    private LocalDateTime createdAt;
}