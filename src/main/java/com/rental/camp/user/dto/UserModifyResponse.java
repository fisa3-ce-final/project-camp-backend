package com.rental.camp.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModifyResponse {
    private String nickname;
    private String phone;
    private String address;
    private String imageUrl;
}
