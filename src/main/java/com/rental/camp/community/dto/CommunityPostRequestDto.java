package com.rental.camp.community.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CommunityPostRequestDto {
    private Long userId;
    private String title;
    private String content;
    private String category;
    private BigDecimal rating;
    private List<MultipartFile> images;
}